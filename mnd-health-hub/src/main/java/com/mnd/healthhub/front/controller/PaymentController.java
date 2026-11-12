package com.mnd.healthhub.front.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mnd.healthhub.front.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
@Tag(name = "微信支付")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/test")
    @Operation(summary = "支付测试", description = "支付1分钱测试是否能够成功创建微信付款单")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 商家需要生成交易的流水号
            String outTradeNo = "TEST" + System.currentTimeMillis();
            ObjectNode objectNode = paymentService.unifiedOrder(outTradeNo, 1, "测试商品", null);
            result.put("success", true);
            result.put("outTradeNo", outTradeNo);
            result.put("codeUrl", objectNode.get("code_url").asText());
            result.put("message", "下单成功，请使用微信扫码支付");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "下单失败：" + e.getMessage());
        }
        return result;
    }
}
