package com.mnd.healthhub.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.front.controller.form.CreatePaymentForm;
import com.mnd.healthhub.front.controller.form.GetPaymentResultForm;
import com.mnd.healthhub.front.controller.form.OrderPageQueryForm;
import com.mnd.healthhub.front.controller.form.RefundForm;
import com.mnd.healthhub.front.service.TradeOrderService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wechat.pay.java.core.notification.RequestParam;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/front/order")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订单管理", description = "订单相关接口")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @PostMapping
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "创建支付订单", description = "用户购买商品时创建支付订单")
    public R createPayment(@RequestBody @Valid CreatePaymentForm form) {
        // 获取customerId
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Integer buyCount = form.getBuyCount();
        Integer goodsId = form.getGoodsId();

        // 执行下单的核心业务方法
        Map<String, Object> result = tradeOrderService.createPayment(customerId, goodsId, buyCount);
        if (result == null) {
            // 如果是空，一定是触发了风控。
            return R.ok().put("illegal", true);
        } else {
            // 没有触发风控，并且下单成功了。
            return R.ok().put("illegal", false).put("result", result);
        }
    }


    // 微信支付的配置类
    private final Config wechatPayConfig;

    /**
     * 微信支付结果通知接口
     */
    @PostMapping("/pay/notify")
    @Operation(
            summary = "微信支付结果通知",
            description = "接收微信服务器异步发送的支付结果通知，验证签名并更新订单状态"
    )
    public ResponseEntity<Map<String, String>> handlePayNotify(
            @Parameter(hidden = true) HttpServletRequest request,
            // 微信平台的证书序列号，我们通过它找到对应的证书公钥，公钥有了才能验签
            @Parameter(description = "微信平台证书序列号", required = true, example = "25EED1AD3AE64A61A4C0667752575457981E04C6")
            @RequestHeader("Wechatpay-Serial") String serial,
            // 微信用平台证书私钥对整个请求体和其他头信息算出的签名值
            // 我们用微信平台证书的公钥对同样的信息重新算一遍签名
            // 拿计算出来的签名和这个签名对比，一致：就是微信平台发过来的请求
            @Parameter(description = "签名", required = true)
            @RequestHeader("Wechatpay-Signature") String signature,
            // 签名生成时的Unix时间戳，防止重放攻击，通常参与签名计算
            // 重放攻击就是黑客把微信服务器发过的合法支付通知原封不动
            // 地再往你的服务器发一遍，试图骗你重复处理（比如再次给用户发货、加积分）
            @Parameter(description = "时间戳", required = true)
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            // 签名用的随机字符串，防重放攻击，也参与签名计算。
            @Parameter(description = "随机数", required = true)
            @RequestHeader("Wechatpay-Nonce") String nonce) {

        log.info("收到微信支付通知：serial:{},timestamp:{},nonce:{}", serial, timestamp, nonce);

        try {
            // 1. 获取请求体内容
            StringBuilder sb = new StringBuilder();
            // 从request对象中获取一个带有缓冲区的字符输入流，可以读取请求中的数据
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            log.debug("支付通知原始报文：{}", body);

            // 2. 构建RequestParam
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serial)
                    .signature(signature)
                    .timestamp(timestamp)
                    .nonce(nonce)
                    .body(body)
                    .build();

            // 3. 创建通知解析器
            NotificationParser parser = new NotificationParser((RSAAutoCertificateConfig) wechatPayConfig);

            // 4. 解析通知：验证签名并解密通知（底层细节是不需要关心具体怎么验签的，只需要调用parse方法即可。）
            // Transaction 对象是微信支付为我们封装的一个交易结果对象。
            Transaction transaction = parser.parse(requestParam, Transaction.class);

            // 5. 处理支付结果
            log.info("支付通知解析成功，订单号：{}，交易状态：{}，支付金额：{}", transaction.getOutTradeNo(),
                    transaction.getTradeState(), transaction.getAmount().getTotal());

            // 调用service方法，处理我们系统中的业务逻辑（微信平台要求我们采用异步方式）
            // 最好异步，因为应答超时时间是5秒。
            tradeOrderService.handlePayNotify(transaction);

            // 6.返回应答结果（直接应答200，无包体）
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("处理微信支付通知异常", e);
            // 返回应答结果
            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/payment-result")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "查询支付结果", description = "根据商户订单号查询微信支付结果，支付成功同步更新订单状态")
    public R paymentResult(@Valid GetPaymentResultForm form) {
        String outTradeNo = form.getOutTradeNo();
        boolean paid = tradeOrderService.getPaymentResult(outTradeNo);
        return R.ok().put("paid", paid);
    }

    @GetMapping("/list")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "分页查询", description = "分页查询客户自己的订单列表", operationId = "orderPageQuery")
    public R list(@ParameterObject @Valid OrderPageQueryForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        params.put("customerId", customerId);
        PageResult<Map<String, Object>> pageResult = tradeOrderService.queryPage(params);
        return R.ok().put("pageResult", pageResult);
    }

    @PutMapping("/refund")
    @Operation(summary = "申请退款", description = "根据订单ID发起退款申请")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R refund(@RequestBody @Valid RefundForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        tradeOrderService.refund(customerId, form.getOrderId());
        return R.ok("退款申请已提交");
    }


    /**
     * 微信退款结果通知接口
     */
    @PostMapping("/refund/notify")
    @Operation(summary = "微信退款结果通知", description = "接收微信服务器异步发送的退款结果通知，验证签名并更新退款状态")
    public ResponseEntity<Map<String, String>> handleRefundNotify(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(description = "微信平台证书序列号", required = true)
            @RequestHeader("Wechatpay-Serial") String serial,
            @Parameter(description = "签名", required = true)
            @RequestHeader("Wechatpay-Signature") String signature,
            @Parameter(description = "时间戳", required = true)
            @RequestHeader("Wechatpay-Timestamp") String timestamp,
            @Parameter(description = "随机数", required = true)
            @RequestHeader("Wechatpay-Nonce") String nonce) {

        log.info("收到微信退款通知，serial: {}, timestamp: {}, nonce: {}", serial, timestamp, nonce);

        try {
            // 1. 获取请求体内容
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            log.debug("退款通知原始报文：{}", body);

            // 2. 构建 RequestParam
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serial)
                    .signature(signature)
                    .timestamp(timestamp)
                    .nonce(nonce)
                    .body(body)
                    .build();

            // 3. 创建通知解析器
            NotificationParser parser = new NotificationParser((RSAAutoCertificateConfig) wechatPayConfig);

            // 4. 解析通知，验证签名并解密数据
            RefundNotification refundNotification = parser.parse(requestParam, RefundNotification.class);

            // 5. 处理退款结果
            String outRefundNo = refundNotification.getOutRefundNo();        // 商户退款单号
            String refundId = refundNotification.getRefundId();              // 微信退款单号
            String outTradeNo = refundNotification.getOutTradeNo();          // 商户订单号
            String refundStatus = refundNotification.getRefundStatus().toString(); // 退款状态

            log.info("退款通知解析成功，退款单号：{}，微信退款单号：{}，退款状态：{}，商户订单号：{}",
                    outRefundNo, refundId, refundStatus, outTradeNo);

            // 6. 调用业务处理逻辑（建议仍然采用异步方式，为了当前接口能够快速应答微信服务器。避免应答超时。）
            tradeOrderService.handleRefundNotify(refundNotification);

            // 7. 返回成功应答（200/204的返回，不需要包体）
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("处理微信退款通知异常", e);
            Map<String, String> response = new HashMap<>();
            response.put("code", "FAIL");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
