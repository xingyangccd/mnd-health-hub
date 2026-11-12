package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.mnd.healthhub.common.FaceAuthUtil;
import com.mnd.healthhub.common.MinIO;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.mapper.*;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final MedExamAppointmentMapper appointmentMapper;
    private final FaceAuthUtil faceAuthUtil;
    private final MinIO minIO;
    private final GoodsSnapshotDao goodsSnapshotDao;
    private final CheckupResultDao checkupResultDao;
    private final TradeOrderMapper tradeOrderMapper;
    private final MedExamReportMapper medExamReportMapper;

    @Override
    public List<Map<String, Object>> getByOrderId(Integer orderId) {
        return appointmentMapper.selectByOrderId(orderId);
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> param) {
        long total = appointmentMapper.selectPageCountForMis(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = appointmentMapper.selectPageListForMis(param);
        }
        return PageResult.of(records, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Integer[] ids) {
        int rows = appointmentMapper.deleteByIds(ids);
        if (rows != ids.length) {
            throw new HisException("删除预约数据失败", 500);
        }
        log.info("预约数据删除成功，删除了{}条", ids.length);
    }

    @Override
    public int hasAppointmentInToday(Map<String, Object> params) {
        Map<String, Object> map = appointmentMapper.selectAppointInToday(params);
        if (CollUtil.isEmpty(map)) {
            log.info("今日无预约，参数：{}", params);
            return 0;
        }

        // map不是空。身份信息是正确的，继续判断status
        int status = MapUtil.getInt(map, "status");
        if (status == 1) {
            log.info("今日有预约，未签到，预约信息：{}", params);
            return 1;
        } else {
            log.warn("今日有预约，已签到/已结束，预约信息：{}", params);
            return -1;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkin(Map<String, Object> params) {
        // 1. 从参数map当中获取体检人的参数信息
        String idCardNo = MapUtil.getStr(params, "idCardNo");
        String patientName = MapUtil.getStr(params, "patientName");
        String gender = IdcardUtil.getGenderByIdCard(idCardNo) == 1 ? "男" : "女";
        String photo_1 = MapUtil.getStr(params, "photo_1");
        String photo_2 = MapUtil.getStr(params, "photo_2");

        log.info("开始签到，身份证号={}，姓名={}，性别={}", idCardNo, patientName, gender);

        // 2. 调用人脸识别进行人脸比对，以及同步人员库等操作。
        boolean result = faceAuthUtil.verifyFaceModel(patientName, idCardNo, gender, photo_1, photo_2);
        if (!result) {
            log.warn("人脸认证失败，签到终止，身份证号={}", idCardNo);
            return false;
        }

        // 3. 将base64编码的字符串图片上传到minio服务器
        String filename = idCardNo + "_" + System.currentTimeMillis() + ".jpg";
        String path = "checkin/" + filename;
        minIO.uploadImage(path, photo_2);
        log.info("签到的现场照片已经上传到minio服务器，path={}", path);

        // 4. 更新预约的状态
        int rows = appointmentMapper.updateForCheckin(params);
        if (rows != 1) {
            log.error("更新预约状态失败，影响行数={}", rows);
            throw new HisException("更新预约状态失败");
        }
        log.info("更新预约状态成功，身份证号={}", idCardNo);

        // 5. 查询预约编号+商品快照id
        Map<String, Object> map = appointmentMapper.selectAppointNoAndSnapshotId(params);
        if (CollUtil.isEmpty(map)) {
            log.error("查询预约信息失败，身份证号：{}", idCardNo);
            throw new HisException("查询预约信息失败");
        }
        String appointmentNo = MapUtil.getStr(map, "appointmentNo");
        String snapshotId = MapUtil.getStr(map, "snapshotId");
        log.info("查询到的预约信息：预约编号={}，快照ID={}", appointmentNo, snapshotId);

        // 6. 根据快照ID和性别查询对应的体检项目
        List<Map> checkup = goodsSnapshotDao.searchCheckup(snapshotId, gender);
        log.info("查询到所有的体检项目数量：{}", checkup != null ? checkup.size() : 0);

        // 7. 为体检人初始化体检结果的空壳数据。
        boolean saveResult = checkupResultDao.insert(appointmentNo, checkup);
        if (!saveResult) {
            log.error("初始化体检结果失败：预约编号={}", appointmentNo);
            throw new HisException("初始化体检结果失败");
        }
        log.info("体检结果初始化成功：预约编号={}", appointmentNo);

        // 能够走到这里说明签到成功了。
        return true;
    }

    @Override
    public Map<String, Object> loadGuidanceInfo(Integer id) {
        log.info("开始加载导诊信息，预约记录ID：{}", id);

        // 根据预约ID查询体检人的摘要信息。
        Map<String, Object> map = appointmentMapper.selectSummaryById(id);
        if (CollUtil.isEmpty(map)) {
            log.warn("未查询到预约信息，预约ID = {}", id);
            throw new HisException("预约记录不存在");
        }

        // 从查询结果 map 当中获取快照ID、性别、预约编号
        String snapshotId = MapUtil.getStr(map, "snapshotId");
        String gender = MapUtil.getStr(map, "gender");
        String appointmentNo = MapUtil.getStr(map, "appointmentNo");
        log.info("预约信息：预约编号={}，性别={}，快照ID={}", appointmentNo, gender, snapshotId);

        // 将预约编号生成二维码
        QrConfig qrConfig = new QrConfig(); // 二维码的配置信息。
        qrConfig.setWidth(100);
        qrConfig.setHeight(100);
        qrConfig.setMargin(0);
        String qrCodeBase64 = QrCodeUtil.generateAsBase64(appointmentNo, qrConfig, "jpg");
        map.put("qrCodeBase64", qrCodeBase64);
        log.info("二维码生成成功，预约编号={}", appointmentNo);

        // 根据商品快照ID以及性别获取适合的对应的体检项目。
        // 注意：返回的List集合中的Map和Map之间有可能重复。
        List<Map> checkupList = goodsSnapshotDao.searchCheckup(snapshotId, gender);
        log.info("查询到体检项目的数量：{}", checkupList != null ? checkupList.size() : 0);

        // 对以上查询的所有的体检项目进行去重操作，而且还要保证顺序不乱。
        // List集合中有map1和map2，我们要检查map1和map2是否一样，如果一样，去掉一个。
        // map1: name=采血检查  place=采血室
        // map2: name=采血检查  place=采血室
        // map1的 name+place 和 map2的 name+place 对应一致，则需要去重。
        Set<Map> checkupSet = new LinkedHashSet<>(); // 既可以去重，又可以保证顺序。
        if (CollUtil.isNotEmpty(checkupList)) {
            for (Map item : checkupList) {
                Map<String, String> temp = new HashMap<>();
                temp.put("place", MapUtil.getStr(item, "place"));
                temp.put("name", MapUtil.getStr(item, "name"));
                // map1: {"place": "三楼A1室", "name":"采血检查"}
                // map2: {"place": "三楼A1室", "name":"采血检查"}
                checkupSet.add(temp); // 去重
            }
        }

        map.put("checkupSet", checkupSet);
        log.info("导诊信息加载完毕，预约编号={}", appointmentNo);

        return map;
    }

    // 通用的更新预约记录状态的业务方法。
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAppointmentStatus(Map<String, Object> params) {
        String appointmentNo = MapUtil.getStr(params, "appointmentNo");
        Integer status = MapUtil.getInt(params, "status");
        log.info("开始更新预约状态，预约编号={}，状态={}", appointmentNo, status);

        // 1. 更新预约记录的状态
        int rows = appointmentMapper.updateStatusByAppointNo(params);
        if(rows != 1) {
            log.error("更新预约状态失败，预约编号={}", appointmentNo);
            return false;
        }

        // 该业务方法是一个通用的业务方法。因此status状态值不一定是3
        // 如果状态值不是3，后续的操作就没必要了，直接返回true。
        if(status != 3){
            return true;
        }

        // 2. 检查该预约记录对应的订单是否已经消费完毕。
        Map<String, Object> orderCheck = tradeOrderMapper.selectOrderIsFinished(appointmentNo);
        if(CollUtil.isEmpty(orderCheck)) {
            log.warn("未找到关联的订单，预约编号={}", appointmentNo);
            return false;
        }

        Integer orderId = MapUtil.getInt(orderCheck, "orderId");
        Integer totalCount = MapUtil.getInt(orderCheck, "n1");
        Integer finishedCount = MapUtil.getInt(orderCheck, "n2");

        if(totalCount != null && totalCount.equals(finishedCount)) {
            // 3. 将订单状态更新为 已完成
            rows = tradeOrderMapper.updateStatus(orderId, 6);
            if(rows != 1) {
                log.error("更新订单状态失败，订单ID：{}，预约编号：{}", orderId, appointmentNo);
                return false;
            }
        }

        // 4.根据预约编号查找报告ID
        String reportId = checkupResultDao.findIdByAppointNo(appointmentNo);
        if(reportId == null) {
            log.error("未找到体检结果，预约编号：{}", appointmentNo);
            return false;
        }
        params.put("reportId", reportId);

        // 5.根据预约编号查找预约ID
        int appointmentId = appointmentMapper.findIdByAppointNo(appointmentNo);
        params.put("appointmentId", appointmentId);

        // 6.插入体检报告的初始记录
        rows = medExamReportMapper.insert(params);
        if(rows != 1) {
            log.error("保存体检报告初始记录失败，预约编号：{}，报告ID：{}", appointmentNo, reportId);
            return false;
        }

        return true;
    }

    @Override
    public Map<String, Object> getByAppointmentNo(String appointmentNo) {
        log.info("查询预约信息，预约编号={}", appointmentNo);
        // 根据预约编号查询体检人的信息
        Map<String, Object> map = appointmentMapper.selectByAppointmentNo(appointmentNo);
        // 判断返回值是否为空
        if(CollUtil.isEmpty(map)) {
            log.warn("未找到预约信息，预约编号={}", appointmentNo);
            throw new HisException("该体检人未预约", 400);
        }
        // 获取状态status
        int status = MapUtil.getInt(map, "status");

        if(status == 1){
            // 如果状态status == 1：预约未签到
            log.warn("预约未签到，预约编号={}", appointmentNo);
            throw new HisException("该体检人未签到", 400);
        }else if(status == 4) {
            // 如果状态status == 4：预约已取消
            log.warn("预约已取消，预约编号={}", appointmentNo);
            throw new HisException("该体检人预约已取消", 400);
        } // status == 2 或者 status == 3 的情况都是允许的。
        return map;
    }
}
















