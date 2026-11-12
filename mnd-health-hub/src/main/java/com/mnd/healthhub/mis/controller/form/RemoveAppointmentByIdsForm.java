package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "MIS端删除预约请求参数")
public class RemoveAppointmentByIdsForm {

    @Schema(description = "预约ID数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "ids不能为空")
    private Integer[] ids;
}