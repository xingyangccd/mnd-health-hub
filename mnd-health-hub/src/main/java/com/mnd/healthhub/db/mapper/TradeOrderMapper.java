package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.TradeOrderEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【trade_order(交易订单表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.TradeOrderEntity
 */
public interface TradeOrderMapper {
    /**
     * 根据客户id查询客户的订单统计信息
     *
     * @param id 客户id
     * @return 订单统计信息
     */
    Map<String, Object> selectOrderStatistics(Integer id);

    /**
     * 判断这个客户是否已经达到了当天下单的限制。
     *
     * @param customerId 客户id
     * @return true表示已达到限制，false表示未达到限制。
     */
    boolean isCustomerReachedDailyLimit(@Param("customerId") Integer customerId);

    /**
     * 关闭订单：查看有没有超时未支付的订单，有的话，将订单状态修改为2
     *
     * @return 关单数量
     */
    int closeOrder();

    /**
     * 保存订单
     *
     * @param order 订单
     * @return 1表示成功
     */
    int insert(TradeOrderEntity order);

    /**
     * 关闭超时未支付的订单
     *
     * @param outTradeNo 订单流水号
     * @return 1表示关单成功
     */
    int closeOrderByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 更新订单的 微信支付订单号以及订单的状态
     *
     * @param outTradeNo    商户创建的订单流水号
     * @param transactionId 微信支付订单号（微信平台返回的）
     * @param status        订单的状态
     * @return 1表示更新成功
     */
    int updatePayment(@Param("outTradeNo") String outTradeNo, @Param("transactionId") String transactionId, @Param("status") Integer status);

    /**
     * 根据订单流水号获取订单信息
     *
     * @param outTradeNo 订单流水号
     * @return 订单信息
     */
    TradeOrderEntity selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 根据订单交易流水号获取客户id
     *
     * @param outTradeNo 订单交易流水号
     * @return 客户id
     */
    Integer findCustomerIdByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * 查询符合条件的订单记录
     *
     * @param params 查询条件
     * @return 订单列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> params);

    /**
     * 查询符合条件订单记录条数
     *
     * @param params 查询条件
     * @return 记录条数
     */
    long selectPageCount(Map<String, Object> params);

    /**
     * 根据订单id获取退款单号
     *
     * @param orderId 订单id
     * @return 退款单号
     */
    String findOutRefundNoByOrderId(@Param("orderId") Integer orderId);

    /**
     * 根据订单id和客户id获取该订单的支付信息，包括：transaction_id 和 total_amount
     *
     * @param orderId    订单id
     * @param customerId 客户id
     * @return transaction_id 和 total_amount
     */
    Map<String, Object> findTransactionInfoByOrderIdAndCustomerId(@Param("orderId") Integer orderId, @Param("customerId") Integer customerId);

    /**
     * 根据退款单号以及订单id修改订单的退款单号
     *
     * @param orderId  订单id
     * @param refundNo 退款单号
     * @return 1表示更新成功
     */
    int updateOutRefundNoByOrderIdAndOutRefundNo(@Param("orderId") Integer orderId, @Param("refundNo") String refundNo);

    /**
     * 将订单的状态更新为已退款。
     *
     * @param outTradeNo 商家生成的订单流水号
     * @return 1表示更新成功
     */
    int updateStatusByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    /**
     * mis端使用的，查询符合条件的订单记录
     *
     * @param params 查询条件
     * @return 订单列表
     */
    List<Map<String, Object>> selectPageListForMis(Map<String, Object> params);

    /**
     * mis端使用，查询符合条件的订单数量
     *
     * @param params 查询条件
     * @return 记录条数
     */
    long selectPageCountForMis(Map<String, Object> params);

    /**
     * 根据id删除订单。
     *
     * @param orderId 订单id
     * @return 1表示删除成功
     */
    int deleteById(@Param("orderId") Integer orderId);

    /**
     * 根据客户id和快照id查询符合条件的记录
     *
     * @param customerId 客户id
     * @param snapshotId 快照id
     * @return 大于0表示该客户拥有该商品快照，等于0表示该客户对该商品快照无权限
     */
    long findByCustomerIdAndSnapshotId(@Param("customerId") Integer customerId, @Param("snapshotId") String snapshotId);

    /**
     * 更新订单状态
     *
     * @param orderId     订单id
     * @param orderStatus 订单状态
     * @return 1表示更新成功
     */
    int updateStatus(@Param("orderId") Integer orderId, @Param("orderStatus") Integer orderStatus);

    /**
     * 判断该客户是否拥有该订单
     *
     * @param orderId    订单id
     * @param customerId 客户id
     * @return true表示该客户拥有该订单。
     */
    boolean existsByOrderIdAndCustomerId(@Param("orderId") Integer orderId, @Param("customerId") Integer customerId);

    /**
     * 查询预约记录对应的订单是否已完成。
     *
     * @param appointmentNo 预约记录编号
     * @return 套餐数量、体检完成数量
     */
    Map<String, Object> selectOrderIsFinished(String appointmentNo);
}




