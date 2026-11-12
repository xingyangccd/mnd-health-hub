package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "今日预约查询请求参数")
public class HasAppointmentInTodayForm {

    @Schema(description = "患者姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "patientName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "patientName内容不正确")
    private String patientName;

    @Schema(description = "性别", example = "男", allowableValues = {"男", "女"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "gender不能为空")
    @Pattern(regexp = "^男$|^女$", message = "gender内容不正确")
    private String gender;

    @Schema(description = "身份证号码", example = "110105199001011234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "idCardNo不能为空")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "身份证号码无效")
    private String idCardNo;
}