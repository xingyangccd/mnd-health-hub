package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.PromotionRuleEntity;

import java.util.List;
import java.util.Map;

public interface PromotionRuleService {

    /**
     * 查询所有的促销规则列表
     *
     * @return 规则列表
     */
    List<Map<String, Object>> listAll();

    /**
     * 分页查询促销规则
     *
     * @param params 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> params);

    /**
     * 保存促销规则
     *
     * @param rule 促销规则
     */
    void save(PromotionRuleEntity rule);

    /**
     * 根据id查询促销规则
     *
     * @param ruleId 规则id
     * @return 促销规则对象
     */
    PromotionRuleEntity getById(Integer ruleId);

    /**
     * 更新促销规则
     *
     * @param rule 要更新的促销规则
     */
    void updateById(PromotionRuleEntity rule);

    /**
     * 根据促销规则id删除促销规则
     * @param ruleId 规则id
     */
    void removeById(Integer ruleId);
}
