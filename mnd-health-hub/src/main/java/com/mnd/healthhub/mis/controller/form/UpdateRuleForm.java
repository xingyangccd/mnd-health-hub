package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "修改规则请求参数")
public class UpdateRuleForm {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1", minimum = "1")
    @NotNull(message = "ruleId不能为空")
    @Min(value = 1, message = "ruleId不能小于1")
    private Integer ruleId;

    @Schema(description = "规则名称，支持字母、数字、中文，长度1-20位", requiredMode = Schema.RequiredMode.REQUIRED, example = "满减规则")
    @NotBlank(message = "ruleName不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{1,20}$", message = "ruleName内容不正确")
    private String ruleName;

    @Schema(description = "规则内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "满100减10元")
    @NotBlank(message = "ruleContent不能为空")
    private String ruleContent;

    @Schema(description = "备注", example = "双十一促销规则")
    private String remark;
}