package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "添加体检结果请求参数")
public class AddCheckupResultForm {

    @Schema(description = "医生姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张伟")
    @NotBlank(message = "doctorName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "doctorName内容不正确")
    private String doctorName;

    @Schema(description = "预约编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    @NotBlank(message = "appointmentNo不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{32}$", message = "appointmentNo内容不正确")
    private String appointmentNo;

    @Schema(description = "体检科室", requiredMode = Schema.RequiredMode.REQUIRED, example = "内科")
    @NotBlank(message = "place不能为空")
    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5]{2,30}$", message = "place内容不正确")
    private String place;

    @Schema(description = "体检项目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "item不能为空")
    private List item;

    @Schema(description = "报告模板", requiredMode = Schema.RequiredMode.REQUIRED, example = "default")
    @NotEmpty(message = "template不能为空")
    private String template;
}