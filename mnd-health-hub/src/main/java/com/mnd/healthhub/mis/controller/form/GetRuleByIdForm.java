package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "根据ID查询规则请求参数")
public class GetRuleByIdForm {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1", minimum = "1")
    @NotNull(message = "ruleId不能为空")
    @Min(value = 1, message = "ruleId不能小于1")
    private Integer ruleId;
}