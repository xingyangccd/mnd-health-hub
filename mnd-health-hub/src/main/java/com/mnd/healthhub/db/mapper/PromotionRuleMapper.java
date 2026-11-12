package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.PromotionRuleEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【promotion_rule(促销规则表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.PromotionRuleEntity
 */
public interface PromotionRuleMapper {
    /**
     * 查询所有的促销规则
     *
     * @return 规则列表
     */
    List<Map<String, Object>> selectAll();

    /**
     * 查询符合条件的促销规则数据
     *
     * @param params 查询条件
     * @return 促销规则列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> params);

    /**
     * 查询符合条件的记录
     *
     * @param params 查询条件
     * @return 记录总数
     */
    long selectPageCount(Map<String, Object> params);

    /**
     * 保存促销规则
     *
     * @param rule 促销规则
     * @return 1表示保存成功
     */
    int insert(PromotionRuleEntity rule);

    /**
     * 根据规则id查询规则信息
     *
     * @param ruleId 规则id
     * @return 规则信息对象
     */
    PromotionRuleEntity selectById(Integer ruleId);

    /**
     * 根据id更新促销规则
     *
     * @param rule 要修改的规则
     * @return 1表示更新成功
     */
    int updateById(PromotionRuleEntity rule);

    /**
     * 根据id删除规则
     *
     * @param ruleId 规则id
     * @return 1表示成功
     */
    int deleteById(Integer ruleId);


}




