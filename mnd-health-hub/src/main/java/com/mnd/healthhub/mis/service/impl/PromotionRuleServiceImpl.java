package com.mnd.healthhub.mis.service.impl;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.PromotionRuleEntity;
import com.mnd.healthhub.db.mapper.MedExamPackageMapper;
import com.mnd.healthhub.db.mapper.PromotionRuleMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.PromotionRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionRuleServiceImpl implements PromotionRuleService {
    private final PromotionRuleMapper promotionRuleMapper;
    private final MedExamPackageMapper medExamPackageMapper;

    @Override
    public List<Map<String, Object>> listAll() {
        return promotionRuleMapper.selectAll();
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> params) {
        long total = promotionRuleMapper.selectPageCount(params);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = promotionRuleMapper.selectPageList(params);
        }
        return PageResult.of(records, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(PromotionRuleEntity rule) {
        int rows = promotionRuleMapper.insert(rule);
        if (rows != 1) {
            throw new HisException("规则保存失败", 500);
        }
        log.info("规则保存成功");
    }

    @Override
    public PromotionRuleEntity getById(Integer ruleId) {
        PromotionRuleEntity rule = promotionRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new HisException("规则不存在", 404);
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(PromotionRuleEntity rule) {
        int rows = promotionRuleMapper.updateById(rule);
        if (rows != 1) {
            throw new HisException("规则更新失败", 500);
        }
        log.info("规则更新成功，规则ID={}", rule.getRuleId());
    }

    // 没有在SQL层控制，在应用层控制的。
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Integer ruleId) {
        Integer count = medExamPackageMapper.selectCountByPromotionId(ruleId);
        if (count > 0) {
            throw new HisException("有套餐在使用该促销规则，无法删除", 400);
        }
        int rows = promotionRuleMapper.deleteById(ruleId);
        if (rows != 1) {
            throw new HisException("促销规则删除失败", 500);
        }
        log.info("促销规则删除成功，规则ID={}", ruleId);
    }
}











