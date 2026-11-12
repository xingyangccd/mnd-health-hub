package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.MedExamReportEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【med_exam_report(体检报告表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.MedExamReportEntity
 */
public interface MedExamReportMapper {

    /**
     * 保存体检报告记录到体检报告表当中（这个时候体检报告还没生成，只是先保存一条记录，供 完成体检 功能使用）
     *
     * @param params 参数
     * @return 1表示保存成功
     */
    int insert(Map<String, Object> params);

    /**
     * 查询符合条件的体检报告数据
     *
     * @param param 查询条件
     * @return 体检报告列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> param);

    /**
     * 查询符合条件的体检报告记录条数
     *
     * @param param 查询条件
     * @return 记录条数
     */
    long selectPageCount(Map<String, Object> param);

    /**
     * 根据ID查询体检报告记录。
     *
     * @param id 主键ID
     * @return 体检报告记录
     */
    Map<String, Object> selectById(Integer id);

    /**
     * 更新体检报告记录
     *
     * @param params 参数
     * @return 1表示更新成功
     */
    int update(Map<String, Object> params);

    /**
     * 查询前50个没有生成体检报告的数据
     *
     * @return 报告ID集合
     */
    List<Integer> select50NonReport();

}




