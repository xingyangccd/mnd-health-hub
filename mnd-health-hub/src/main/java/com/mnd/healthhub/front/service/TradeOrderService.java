package com.mnd.healthhub.front.service;

import com.mnd.healthhub.common.PageResult;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.RefundNotification;

import java.util.Map;

public interface TradeOrderService {

    /**
     * 下单的核心业务方法。
     *
     * @param customerId 客户id
     * @param goodsId    商品id
     * @param quantity   购买数量
     * @return null表示触发风控，非null表示下单成功
     */
    Map<String, Object> createPayment(int customerId, Integer goodsId, Integer quantity);

    /**
     * 处理支付通知：更新订单状态
     *
     * @param transaction 微信平台返回的支付结果对象
     */
    void handlePayNotify(Transaction transaction);

    /**
     * 获取付款结果
     *
     * @param outTradeNo 订单流水号
     * @return true表示已付款
     */
    boolean getPaymentResult(String outTradeNo);

    /**
     * 分页查询订单数据
     *
     * @param params 查询条件
     * @return 分页数据
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> params);

    /**
     * 根据订单id给客户执行退款操作
     *
     * @param customerId 客户id
     * @param orderId    订单id
     */
    void refund(Integer customerId, Integer orderId);

    /**
     * 将订单状态更新为已退款
     *
     * @param refundNotification 退款通知对象
     */
    void handleRefundNotify(RefundNotification refundNotification);

    /**
     * 检查这个订单是否归属于这个客户
     * @param orderId 订单id
     * @param customerId 客户id
     */
    void checkOrderOwnership(Integer orderId, Integer customerId);
}
