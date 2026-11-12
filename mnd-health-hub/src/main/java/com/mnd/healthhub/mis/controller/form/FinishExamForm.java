package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "完成体检请求参数")
public class FinishExamForm {

    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "appointmentNo内容不正确")
    @Schema(description = "预约号", requiredMode = Schema.RequiredMode.REQUIRED, example = "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6")
    private String appointmentNo;

    @NotNull(message = "status不能为空")
    @Range(min = 1, max = 4, message = "status内容不正确")
    @Schema(description = "状态: 1-待体检, 2-体检中, 3-已完成, 4-已取消", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer status;
}