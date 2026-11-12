package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "保存促销规则请求参数")
public class SavePromotionRuleForm {

    @Schema(description = "规则名称，支持字母、数字、中文，长度1-20位", example = "满减规则")
    @NotBlank(message = "ruleName不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{1,20}$", message = "ruleName内容不正确")
    private String ruleName;

    @Schema(description = "规则内容", example = "满100减10元")
    @NotBlank(message = "ruleContent不能为空")
    private String ruleContent;

    @Schema(description = "备注", example = "双十一促销规则")
    private String remark;
}