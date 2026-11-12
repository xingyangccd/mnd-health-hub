package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "保存运单信息请求参数")
public class SaveWaybillForm {

    @Schema(description = "体检报告ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id不能为空")
    @Min(value = 1, message = "id不能小于1")
    private Integer id;

    @Schema(description = "快递单号", example = "SF1234567890", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "^[0-9a-zA-Z]{10,24}$")
    @NotBlank(message = "expressNo不能为空")
    @Pattern(regexp = "^[0-9a-zA-Z]{10,24}$", message = "expressNo内容不正确")
    private String expressNo;

    @Schema(description = "快递日期", example = "2026-09-09", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "yyyy-MM-dd")
    @NotBlank(message = "expressDate不能为空")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "expressDate内容不正确")
    private String expressDate;
}