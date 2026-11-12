package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.common.CheckupReportUtil;
import com.mnd.healthhub.common.MinIO;
import com.mnd.healthhub.common.OcrUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.CheckupResultEntity;
import com.mnd.healthhub.db.mapper.CheckupResultDao;
import com.mnd.healthhub.db.mapper.MedExamAppointmentMapper;
import com.mnd.healthhub.db.mapper.MedExamReportMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.MedExamReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MedExamReportServiceImpl implements MedExamReportService {

    private final MedExamReportMapper medExamReportMapper;
    private final MinIO minIO; // 上传word文件
    private final CheckupReportUtil checkupReportUtil; // 生成体检报告
    private final CheckupResultDao checkupResultDao; // 查询体检结果
    private final MedExamAppointmentMapper medExamAppointmentMapper; // 查询预约信息
    private final OcrUtil ocrUtil;

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> param) {
        long total = medExamReportMapper.selectPageCount(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = medExamReportMapper.selectPageList(param);
        }
        return PageResult.of(records, total);
    }

    /**
     * 创建体检报告
     * 1. 校验报告的状态
     * 2. 检查体检结果的完整性
     * 3. 生成word文件
     * 4. 上传到minio
     * 5. 更新体检报告记录
     *
     * @param id 报告记录的主键ID值
     * @return true表示创建成功，false表示失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createReport(Integer id) {
        long start = System.currentTimeMillis();
        log.info("开始生成体检报告，报告ID：{}", id);

        // 1.校验报告状态
        Map<String, Object> report = medExamReportMapper.selectById(id);
        if (CollUtil.isEmpty(report)) {
            log.error("体检报告记录不存在，ID：{}", id);
            throw new HisException("体检报告记录不存在", 400);
        }

        // 获取报告记录的相关信息
        int appointmentId = MapUtil.getInt(report, "appointmentId");
        String reportId = MapUtil.getStr(report, "reportId");
        int status = MapUtil.getInt(report, "status");
        String date = MapUtil.getStr(report, "examDate");

        // 体检日期的校验：体检后10天内是不允许生成报告的。
        DateTime examDate = DateUtil.parseDate(date); // 体检日期
        DateTime today = new DateTime(DateUtil.today()); // 当前日期
        if (today.offsetNew(DateField.DAY_OF_MONTH, -10).isBefore(examDate)) {
            log.error("生成报告失败，体检日期：{}，当前日期距离体检日期不足10天，报告ID：{}", examDate, id);
            throw new HisException("无法生成10天以内的体检报告");
        }

        // 体检报告的状态校验
        if (status != 1) {
            log.warn("体检报告已生成，ID：{}，状态：{}，当前任务结束", id, status);
            return true;
        }
        log.debug("体检报告状态校验通过，当前状态：未生成");

        // 2. 查询体检报告相关的数据（检查体检结果的完整性）
        // 体检人的信息
        log.info("查询预约数据，预约ID：{}", appointmentId);
        Map<String, Object> reportData = medExamAppointmentMapper.selectDataForReport(appointmentId);
        if (CollUtil.isEmpty(reportData)) {
            log.error("查询预约数据失败，预约ID：{}", appointmentId);
            throw new HisException("获取预约数据失败");
        }

        // 体检人的体检项目+体检结果
        log.info("查询体检结果数据，报告ID：{}", reportId);
        CheckupResultEntity entity = checkupResultDao.findById(reportId);
        if (entity == null) {
            log.error("体检结果不存在，报告ID：{}", reportId);
            throw new HisException("体检结果数据不存在");
        }
        // 体检项目
        List<Map> checkup = entity.getCheckup();
        if (CollUtil.isEmpty(checkup)) {
            // 体检项目为空
            log.warn("体检项目为空，报告ID：{}", reportId);
        }
        reportData.put("checkup", checkup);
        reportData.put("result", entity.getResult());

        // 3.检查体检结果的完整性
        // 从所有的体检项中获取所有的科室名称。
        Set<String> allPlaceSet = new HashSet<>();
        if (CollUtil.isNotEmpty(checkup)) { // 体检项目不为空
            for (Map one : checkup) {
                String place = MapUtil.getStr(one, "place");
                if (StrUtil.isNotBlank(place)) {
                    allPlaceSet.add(place);
                }
            }
        }

        // 获取已录入结果的科室列表
        List<String> completedPlaceList = entity.getPlace();

        // 如果存在未录入结果的科室，发送告警
        if (completedPlaceList == null || completedPlaceList.size() < allPlaceSet.size()) {
            // 计算一下哪些科室没有体检。
            Set<String> missingPlace = new HashSet<>(allPlaceSet);
            if (completedPlaceList != null) {
                // 通过这个方法，计算出 哪些科室 没有录入结果/没有体检...
                missingPlace.removeAll(completedPlaceList);
            }
            log.warn("体检结果不完整，缺失科室：{}，报告ID：{}", String.join(",", missingPlace), reportId);
            // TODO：发送告警给相关的领导（打电话、发短信、发邮件...）
            return false;
        }

        log.info("体检结果完整性校验通过");

        // 4. 生成并上传体检报告
        try {
            // 生成word文件
            log.info("开始生成word文件，报告ID：{}", reportId);
            XWPFDocument document = checkupReportUtil.createReport((HashMap<String, Object>) reportData);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                // 将文档写到输出流当中。（文档已经到输出流当中了。）
                document.write(out);
                out.flush();
                // 将输出流中的数据转换成 byte[]，封装为字节输入流。
                try (InputStream in = new ByteArrayInputStream(out.toByteArray())) {
                    String fileUrl = "report/checkup/" + reportId + ".docx";
                    // 文件上传
                    minIO.uploadWord(fileUrl, in);

                    // 更新报告状态（status：1变成2、file_url：填充）
                    Map<String, Object> updateParam = new HashMap<>();
                    updateParam.put("id", id);
                    updateParam.put("status", 2);
                    updateParam.put("fileUrl", fileUrl);

                    int updateCount = medExamReportMapper.update(updateParam);
                    if (updateCount != 1) {
                        log.error("更新报告状态失败，ID：{}", id);
                        throw new HisException("更新报告状态失败");
                    }
                    log.info("体检报告生成成功，ID：{}，文件路径：{}，耗时：{}", id, fileUrl, (System.currentTimeMillis() - start));
                    return true;
                }
            } finally {
                if (document != null) {
                    document.close();
                }
            }

        } catch (Exception e) {
            log.error("生成体检报告异常：ID:{}", id, e);
            throw new HisException("生成体检报告失败:" + e.getMessage());
        }
    }

    @Override
    public Map<String, String> recognizeWaybill(String imgBase64) {
        return ocrUtil.identifyWaybill(imgBase64);
    }

    @Override
    @Transactional(rollbackFor = HisException.class)
    public void saveWaybill(Map<String, Object> param) {
        int rows = medExamReportMapper.update(param);
        if (rows != 1) {
            throw new HisException("保存运单信息失败", 500);
        }
        log.info("运单信息保存成功，运单号：{}", MapUtil.getStr(param, "expressNo"));
    }
}





















