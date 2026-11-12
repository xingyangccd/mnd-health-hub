package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;

import java.util.Map;

public interface MedExamReportService {
    /**
     * 分页查询体检报告
     *
     * @param param 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> param);

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
    boolean createReport(Integer id);

    /**
     * OCR识别运单的方法
     *
     * @param imgBase64 运单图片
     * @return 运单信息
     */
    Map<String, String> recognizeWaybill(String imgBase64);

    /**
     * 保存运单信息
     *
     * @param param 运单信息
     */
    void saveWaybill(Map<String, Object> param);
}
