package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedDepartmentFlowEntity;

import java.util.List;
import java.util.Map;

public interface FlowRegulationService {

    /**
     * 获取所有的科室名字
     *
     * @return 科室名字列表
     */
    List<String> getDeptNames();

    /**
     * 分页查询科室体检调流记录
     *
     * @param params 查询条件
     * @return 分页数据
     */
    PageResult<MedDepartmentFlowEntity> queryPage(Map<String, Object> params);

    /**
     * 保存科室调流规则
     *
     * @param flow 科室调流规则
     */
    void save(MedDepartmentFlowEntity flow);

    /**
     * 根据ID获取科室调流规则
     *
     * @param id 主键ID
     * @return 科室调流规则
     */
    MedDepartmentFlowEntity getById(Integer id);

    /**
     * 更新科室调流规则
     *
     * @param params 科室调流规则
     */
    void update(Map<String, Object> params);

    /**
     * 批量删除科室调流规则
     *
     * @param ids 调流规则ID数组
     */
    void removeByIds(Integer[] ids);
}
