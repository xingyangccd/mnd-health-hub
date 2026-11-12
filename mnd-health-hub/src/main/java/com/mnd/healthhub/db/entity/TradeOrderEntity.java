package com.mnd.healthhub.db.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName trade_order
 */
@Data
public class TradeOrderEntity {
    private Integer orderId;
    private String outTradeNo;
    private Integer customerId;
    private Integer goodsId;
    private String goodsTitle;
    private BigDecimal goodsPrice;
    private String goodsImage;
    private String goodsDescription;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String transactionId;
    private Integer orderStatus;
    private String snapshotId;
    private String outRefundNo;
    private String createTime;
    private String createDate;
    private String refundTime;
    private String refundDate;
}