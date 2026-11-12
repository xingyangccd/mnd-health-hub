package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.PaymentService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${wechat.pay.v3.mnd-health.mch-id}")
    private String mchId;

    @Value("${wechat.pay.v3.mnd-health.app-id}")
    private String appId;

    @Value("${wechat.pay.v3.mnd-health.notify-url}")
    private String notifyUrl;

    @Value("${wechat.pay.v3.mnd-health.refund-notify-url}")
    private String refundNotifyUrl;

    // 微信支付的核心配置对象。
    private final Config wechatPayConfigBean;

    // 因为创建微信支付单之后返回的是一个JSON对象，因此使用 ObjectMapper 来解析返回值。
    private final ObjectMapper objectMapper;

    @Override
    public ObjectNode unifiedOrder(String outTradeNo, int total, String desc, String timeExpire) {
        try {

            // 创建Native支付专用的service对象（底层就是通过这个对象来发送HTTP请求：调用微信服务器的支付接口。）
            NativePayService nativePayService = new NativePayService.Builder().config(wechatPayConfigBean).build();

            // 创建预支付请求对象，封装请求参数。
            PrepayRequest request = new PrepayRequest();
            request.setAppid(appId);
            request.setMchid(mchId);
            request.setDescription(desc);
            request.setOutTradeNo(outTradeNo);
            request.setNotifyUrl(notifyUrl);

            // 创建总金额对象。
            Amount amount = new Amount();
            //amount.setTotal(total); // 生产环境下的。
            amount.setTotal(1); // 测试环境下的。（1分钱）
            amount.setCurrency("CNY");
            request.setAmount(amount);

            // 对于请求来说，过期时间可以指定，也可以为null，所以要判断。
            if (StrUtil.isNotEmpty(timeExpire)) {
                // 只要不为空，表示商家已经设置交易的超时时间。
                request.setTimeExpire(timeExpire);
            }
            log.info("创建Native支付订单，订单号：{}，金额：{}分", outTradeNo, amount.getTotal());

            // 发送HTTP请求，调用创建微信付款单的接口。生成付款单。
            PrepayResponse response = nativePayService.prepay(request);

            // 生成需要返回的JSON对象。
            ObjectNode result = objectMapper.createObjectNode();
            result.put("code_url", response.getCodeUrl());
            result.put("out_trade_no", outTradeNo);

            log.info("Native支付订单创建成功，订单号：{}", outTradeNo);

            // 返回结果
            return result;
        } catch (Exception e) {
            log.error("创建Native支付订单失败，订单号：{}", outTradeNo, e);
            throw new HisException("创建微信付款单失败：" + e.getMessage(), e);
        }
    }

    // 调用微信平台提供的“查询付款结果”的接口，查看是否已付款成功。
    // 支付成功：返回 transactionId
    // 支付失败：返回 null
    @Override
    public String getPaymentResult(String outTradeNo) {
        try {
            // 创建service对象，使用这个对象发送请求的。
            NativePayService nativePayService = new NativePayService.Builder().config(wechatPayConfigBean).build();

            // 封装请求数据
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(mchId);
            request.setOutTradeNo(outTradeNo);

            // 发送请求，查询是否已经付款了。获取付款结果。
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);

            // 获取支付结果
            String tradeState = transaction.getTradeState() != null ? transaction.getTradeState().toString() : "UNKNOWN";

            log.info("查询支付结果，订单号：{}，状态：{}", outTradeNo, tradeState);

            // 支付成功返回 transaction_id，否则返回null
            if ("SUCCESS".equals(tradeState)) {
                String transactionId = transaction.getTransactionId();
                log.info("订单{}支付成功，微信支付单ID：{}", outTradeNo, transactionId);
                return transactionId;
            } else {
                log.info("订单{}支付状态：{}", outTradeNo, tradeState);
                return null;
            }

        } catch (Exception e) {
            log.error("查询支付结果失败，订单号：{}", outTradeNo, e);
            return null;
        }
    }

    // 调用微信支付平台的退款接口，进行退款操作。
    @Override
    public String refund(String transactionId, Integer refund, Integer total) {
        // 1. 生成退款流水号（商家负责生成）
        String outRefundNo = IdUtil.simpleUUID().toUpperCase();

        // 2. 构建退款请求
        CreateRequest request = new CreateRequest();
        request.setTransactionId(transactionId); // 微信支付单id
        request.setOutRefundNo(outRefundNo); // 商家生成的退款流水号
        request.setNotifyUrl(refundNotifyUrl); // 回调URL。

        // 创建金额对象
        AmountReq amount = new AmountReq(); // 金额对象
        amount.setRefund(Long.valueOf(refund)); // 退款金额
        amount.setTotal(Long.valueOf(total)); // 订单本身的金额
        amount.setCurrency("CNY"); // 币种
        request.setAmount(amount);

        log.info("发起微信退款，原微信支付单号：{}，退款单号：{}，退款金额：{}，原金额：{}",
                transactionId, outRefundNo, refund, total);

        // 3. 调用微信平台的退款服务
        RefundService refundService = new RefundService.Builder().config(wechatPayConfigBean).build();
        Refund response = refundService.create(request); // 发送POST请求给微信服务器了。

        // 4. 处理响应
        String status = response.getStatus() != null ? response.getStatus().toString() : "UNKNOWN";
        log.info("微信退款响应，商家的退款单号：{}，状态：{}，微信退款单号：{}",
                outRefundNo, status, response.getRefundId());

        if("PROCESSING".equals(status) || "SUCCESS".equals(status)) {
            return outRefundNo;
        }

        log.warn("退款状态异常，退款单号：{}，状态：{}", outRefundNo, status);

        return null;
    }


}




















