package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "签到预约请求参数")
public class CheckinAppointmentForm {

    @Schema(description = "身份证号码", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101199001011234")
    @NotBlank(message = "idCardNo不能为空")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "身份证号码无效")
    private String idCardNo;

    @Schema(description = "体检人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "patientName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "patientName内容不正确")
    private String patientName;

    @Schema(description = "现场拍摄照片（Base64编码，含Data URI前缀）", requiredMode = Schema.RequiredMode.REQUIRED, example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    @NotBlank(message = "photo_1不能为空")
    private String photo_1;

    @Schema(description = "证件照（Base64编码，含Data URI前缀）", requiredMode = Schema.RequiredMode.REQUIRED, example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    @NotBlank(message = "photo_2不能为空")
    private String photo_2;
}