package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName promotion_rule
 */
@Data
public class PromotionRuleEntity {
    private Integer ruleId;

    private String ruleName;

    private String ruleContent;

    private String remark;
}