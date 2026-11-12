package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.MedDepartmentFlowEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【med_department_flow(科室流量调控表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.MedDepartmentFlowEntity
 */
public interface MedDepartmentFlowMapper {
    /**
     * 查询所有科室的名字
     *
     * @return 科室名字列表
     */
    List<String> selectDeptNames();

    /**
     * 查询符合条件的科室限流规则
     *
     * @param params 查询条件
     * @return 限流规则列表
     */
    List<MedDepartmentFlowEntity> selectPageList(Map<String, Object> params);

    /**
     * 查询符合条件的科室限流规则总记录数
     *
     * @param params 查询条件
     * @return 总记录条数
     */
    long selectPageCount(Map<String, Object> params);

    /**
     * 保存科室调流规则
     *
     * @param flow 科室调流规则
     * @return 1表示成功
     */
    int insert(MedDepartmentFlowEntity flow);

    /**
     * 根据id查询科室调流规则
     *
     * @param id 主键ID
     * @return 科室调流规则
     */
    MedDepartmentFlowEntity selectById(Integer id);

    /**
     * 更新科室调流规则
     *
     * @param params 科室调流规则
     * @return 1表示更新成功
     */
    int update(Map<String, Object> params);

    /**
     * 更新科室门口当前人数
     * 通用方法：可以一次将每个科室门口的人数统一更新为0，也可以将某个科室当前人数更新为指定值。
     *
     * @param params 参数
     * @return 大于0表示更新成功
     */
    int updateCurrentCount(Map<String, Object> params);

    /**
     * 根据权重值获取推荐列表（适合于自动调流）
     *
     * @return 推荐列表
     */
    List<Map<String, Object>> selectRecommendationsWithWeight();

    /**
     * 根据优先级获取推荐列表（适合于手动调流）
     *
     * @return 推荐列表
     */
    List<Map<String, Object>> selectRecommendationsWithPriority();

    /**
     * 查询所有的科室
     *
     * @return 科室列表
     */
    List<Map<String, Object>> selectAllDept();

    /**
     * 批量删除科室调流规则
     *
     * @param ids 科室调流规则ID数组
     * @return 删除的个数
     */
    int deleteByIds(@Param("ids") Integer[] ids);

}




