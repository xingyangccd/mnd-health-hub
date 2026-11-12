package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "查询体检项目请求参数")
public class GetCheckupForm {

    @Schema(description = "预约编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;

    @Schema(description = "体检科室", requiredMode = Schema.RequiredMode.REQUIRED, example = "内科")
    @NotBlank(message = "place不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{2,30}$", message = "place内容不正确")
    private String place;
}