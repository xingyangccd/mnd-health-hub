package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "加载导诊信息请求参数")
public class LoadGuidanceInfoForm {

    @Schema(description = "预约记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "id不能为null")
    @Min(value = 1, message = "id不能小于1")
    private Integer id;
}