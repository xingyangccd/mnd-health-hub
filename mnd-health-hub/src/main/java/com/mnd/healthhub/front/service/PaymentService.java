package com.mnd.healthhub.front.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 微信支付业务，它的作用是：项目调用微信服务器接口的代码都在这个类当中，一个方法通常是一个HTTP请求
 * 调用微信服务器的支付接口：其本质就是发送HTTP请求。（只不过API封装了，看不到HttpClient相关的类。）
 */
public interface PaymentService {

    /**
     * 微信支付统一下单接口 - Native支付（PC扫码支付）
     * 注意：这个方法是调用微信服务器的创建微信支付单的接口来创建微信支付单。
     * 这个方法不是创建我们项目中的订单，这个方法是我们创建订单的其中一步。
     *
     * @param outTradeNo 商户订单号（唯一：订单流水号，商家生成。）
     * @param total 订单总金额，单位：分
     * @param desc 商品描述
     * @param timeExpire 订单过期时间（格式不能随便写，日期时间格式必须严格遵守微信的规定：ISO8601格式，可以为null）
     * @return ObjectNode：包含 code_url等信息的JSON对象【code_url是微信服务器官方响应回来的，我们通过这个url来生成二维码】
     */
    ObjectNode unifiedOrder(String outTradeNo, int total, String desc, String timeExpire);

    /**
     * 获取支付结果
     *
     * @param outTradeNo 订单流水号
     * @return 支付结果
     */
    String getPaymentResult(String outTradeNo);

    /**
     * 退款接口
     *
     * @param transactionId 微信支付单id
     * @param refund        退款金额
     * @param total         订单总金额
     * @return 退款状态是PROCESSING时，返回退款交易流水单号，否则返回null
     */
    String refund(String transactionId, Integer refund, Integer total);
}
