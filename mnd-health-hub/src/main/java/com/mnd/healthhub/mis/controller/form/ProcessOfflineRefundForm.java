package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "线下退款请求参数")
public class ProcessOfflineRefundForm {

    @NotBlank(message = "订单流水号不能为空")
    @Size(min = 16, max = 32, message = "订单流水号长度应在16-32位之间")
    @Pattern(regexp = "^[A-F0-9]+$", message = "订单流水号必须是十六进制字符（0-9, A-F）")
    @Schema(description = "订单流水号（十六进制字符串）", requiredMode = Schema.RequiredMode.REQUIRED, example = "202608291234567890ABCDEF", minLength = 16, maxLength = 32, pattern = "^[A-F0-9]+$")
    private String outTradeNo;
}