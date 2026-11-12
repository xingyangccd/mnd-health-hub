package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "同步支付结果请求参数")
public class SyncPaymentResultForm {

    @NotEmpty(message = "outTradeNos不能为空")
    @Schema(description = "商户订单号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"20260829001\", \"20260829002\"]")
    private String[] outTradeNos;
}