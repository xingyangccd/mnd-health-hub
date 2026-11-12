package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "切换调流模式请求参数")
public class SwitchFlowRegulationModeForm {

    @Schema(description = "调流模式（true-自动调流，false-手动调流）", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "mode不能为空")
    private Boolean mode;
}