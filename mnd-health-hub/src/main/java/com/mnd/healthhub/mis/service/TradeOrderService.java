package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;

import java.util.Map;

public interface TradeOrderService {

    /**
     * 分页查询订单记录
     *
     * @param params 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> params);

    /**
     * 同步付款结果
     *
     * @param outTradeNos 商家订单流水号数组，支持同步多个订单。
     * @return 同步成功的记录条数
     */
    int syncPaymentResult(String[] outTradeNos);

    /**
     * 根据id删除订单
     *
     * @param orderId 订单id
     */
    void removeById(Integer orderId);

    /**
     * 线下退款功能（需要提供订单流水号。）
     *
     * @param outTradeNo 订单流水号
     */
    void processOfflineRefund(String outTradeNo);
}
