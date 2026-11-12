package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.common.MinIO;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedExamPackageEntity;
import com.mnd.healthhub.db.mapper.MedExamPackageMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.MedExamPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MedExamPackageServiceImpl implements MedExamPackageService {

    private final MedExamPackageMapper medExamPackageMapper;
    private final MinIO minIO;

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> param) {
        long total = medExamPackageMapper.selectPageCount(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = medExamPackageMapper.selectPageList(param);
        }
        return PageResult.of(records, total);
    }

    @Override
    public String uploadImg(MultipartFile file) {
        // 获取原始文件的扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            // fd1s2a1f23dsa123f1d23sa.jpeg【.jpeg/.jpg/.png】
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 一定要注意：文件传到minio上的时候，文件名不能重复，文件名重复就会覆盖。
        String fileName = IdUtil.simpleUUID() + extension;
        String path = "front/goods/" + fileName;
        minIO.uploadImage(path, file);
        return path;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(MedExamPackageEntity goods) {
        // 计算商品的md5Hash（根据商品的关键信息计算哈希值）
        String md5Hash = generateMd5Hash(goods);
        goods.setMd5Hash(md5Hash);
        // 判断套餐是否在数据库中已存在
        MedExamPackageEntity entity = medExamPackageMapper.selectByMd5Hash(md5Hash);
        if (entity != null) {
            throw new HisException("套餐已存在", 409);
        }
        // 保存套餐
        int rows = medExamPackageMapper.insert(goods);
        if (rows != 1) {
            throw new HisException("套餐保存失败", 500);
        }
        log.info("体检套餐保存成功：packageCode={}", goods.getPackageCode());
    }

    @Override
    public Map<String, Object> getById(Integer id) {
        Map<String, Object> map = medExamPackageMapper.selectById(Map.of("id", id));
        if (CollUtil.isEmpty(map)) {
            throw new HisException("套餐不存在", 404);
        }
        // map是数据库的查询结果，需要将数据库中查询出来的json字符串转换成json对象。返回给前端系统。
        // 把数据库当中查询的json字符串转换成json对象，然后再存储到map当中
        String[] jsonFields = {"departmentExam", "labExam", "medicalExam", "otherExam", "tags"};
        for (String key : jsonFields) {
            // 通过key获取value
            String value = MapUtil.getStr(map, key);
            if (StrUtil.isNotBlank(value)) {
                // key 重复，value会自动覆盖。
                map.put(key, JSONUtil.parseArray(value));
            }
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "goods", key = "#goods.id") // #goods.id是SpEL语法。
    public void update(MedExamPackageEntity goods) {
        String md5Hash = generateMd5Hash(goods);
        goods.setMd5Hash(md5Hash);
        int rows = medExamPackageMapper.update(goods);
        if (rows != 1) {
            throw new HisException("套餐更新失败", 500);
        }
        log.info("套餐更新成功：packageCode={}", goods.getPackageCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "goods", key = "#goodsId")
    public void uploadExamItems(Integer goodsId, MultipartFile file) {

        List<Map<String, String>> list = new ArrayList<>();

        // 解析excel文件
        try(BufferedInputStream in = new BufferedInputStream(file.getInputStream());
            // 获取整个excel文件这个工作簿
            XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            // 获取工作簿当中的第一个sheet表格。
            XSSFSheet sheet = workbook.getSheetAt(0);
            // 获取表格中总行数
            int lastRowNum = sheet.getLastRowNum();
            // 从第二行开始解析：第一行是表头
            for (int i = 1; i <= lastRowNum; i++) { // i 就是行号。
                XSSFRow row = sheet.getRow(i);
                if(row == null) continue;
                // 到这里row不是空，表示可以从这一行上获取单元格
                String place = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String item = getCellValue(row.getCell(2));
                String type = getCellValue(row.getCell(3));
                String code = getCellValue(row.getCell(4));
                String sex = getCellValue(row.getCell(5));
                String value = getCellValue(row.getCell(6));
                String template = getCellValue(row.getCell(7));

                // 这个条件成立代表excel文件中的这一行是一个空白行。
                if(StringUtils.isAllBlank(place, name, item, type, code, sex, value, template)) continue;

                // 将数据封装到Map当中
                Map<String, String> examItem = new LinkedHashMap<>(); // 有序的（excel文件中解析出来顺序不变。）
                examItem.put("place", place);
                examItem.put("name", name);
                examItem.put("item", item);
                examItem.put("type", type);
                examItem.put("code", code);
                examItem.put("sex", sex);
                examItem.put("value", value);
                examItem.put("template", template);

                list.add(examItem);
            }
            if(list.isEmpty()){
                throw new HisException("Excel文件为空或格式不正确", 400);
            }
        }catch (Exception e){
            log.error("解析Excel失败", e);
            throw new HisException("解析Excel出错了：" + e.getMessage(), 500);
        }

        // 将excel文件上传到minio服务器
        // 一个套餐对应一个excel文件。因此文件名采用套餐的id。底层会产生覆盖。
        String path = "mis/goods/exam_items/" + goodsId + ".xlsx";
        minIO.uploadExcel(path, file);

        // 根据id查询套餐对象
        MedExamPackageEntity goods = medExamPackageMapper.selectEntityById(goodsId);
        // 将上面从excel中获取到的List集合转换成“JSON数组字符串”，这个字符串是可以插入到 DB当中的。
        String examItems = JSONUtil.parseArray(list).toString();
        goods.setExamItems(examItems); // 一定要关联，是为了生成最新的哈希值

        // 重新生成md5Hash哈希值
        String md5Hash = generateMd5Hash(goods);

        // 更新套餐
        Map<String, Object> params = Map.of("id", goodsId, "examItems", examItems, "md5Hash", md5Hash);
        int rows = medExamPackageMapper.updateExamItems(params);
        if(rows != 1){
            throw new HisException("更新具体的体检项目失败", 500);
        }
        log.info("成功更新了具体的体检项目：id={}", goodsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "goods", key = "#params.get('id')", condition = "#params.get('status') == false")
    public void updateStatus(Map<String, Object> params) {
        int rows = medExamPackageMapper.updateStatus(params);
        if (rows != 1) {
            throw new HisException("套餐状态更新失败", 500);
        }
        log.info("套餐状态更新成功，最新状态：{}", MapUtil.getStr(params, "status"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Integer[] ids) {
        if(ArrayUtil.isEmpty(ids)) {
            throw new HisException("ids不能为空", 400);
        }
        // 获取要删除的图片路径
        List<String> images = medExamPackageMapper.selectImagesByIds(ids);
        // 删除套餐
        int rows = medExamPackageMapper.deleteByIds(ids);
        if(rows != ids.length){
            throw new HisException("套餐删除失败", 500);
        }

        // 逐个删除redis中缓存的套餐
        for(Integer id: ids){
            cacheEvictGoods(id);
        }

        // 都删除成功了，但是事务没有提交，必须事务提交了，才能异步删除minio上的图片。
        // 事务同步器。
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 当前事务提交之后，这里的代码才会执行。
                        // 异步删除封面图片
                        asyncDeleteCoverImages(images);
                    }
                }
        );
    }

    @CacheEvict(cacheNames = "goods", key = "#id")
    public void cacheEvictGoods(Integer id){}

    /**
     * 异步删除套餐的封面图片
     * @param images 要删除的封面图片
     */
    @Async("taskExecutor")
    public void asyncDeleteCoverImages(List<String> images) {
        if(CollUtil.isEmpty(images)) {
            log.warn("图片集合为空，无需删除");
            return;
        }
        // 删除图片
        List<String> failedImages = new ArrayList<>();
        images.forEach(image -> {
            try {
                minIO.deleteFile(image);
                log.info("图片删除成功：{}", image);
            }catch (Exception e){
                log.error("图片删除失败：{}", image, e);
                // 将删除失败的图片路径保存起来。
                failedImages.add(image);
            }
        });

        if(!failedImages.isEmpty()) {
            // 可以将失败的图片路径记录到数据库表当中，供后期定时任务重试删除。
            log.error("以下图片删除失败：{}", failedImages);
        }
    }

    // 将来在这里写一个方法，这个方法可以获取单元格当中的数据（并且以字符串的形式返回）
    private String getCellValue(XSSFCell cell) {
        if(cell == null) return "";
        return switch (cell.getCellType()){
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    // 根据商品的关键信息生成哈希值
    private String generateMd5Hash(MedExamPackageEntity goods) {
        // 将商品实体对象转换成json对象。
        JSONObject json = JSONUtil.parseObj(goods);
        // 将一些非关键属性删除掉。
        json.remove("id");
        json.remove("categoryId");
        json.remove("salesVolume");
        json.remove("status");
        json.remove("md5Hash");
        json.remove("createTime");
        json.remove("updateTime");
        // 将JSONObject转换成json字符串。
        return MD5.create().digestHex(json.toString()).toUpperCase();
    }
}
