package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.PaymentService;
import com.mnd.healthhub.mis.service.TradeOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("misOrderService")
@Slf4j
@RequiredArgsConstructor
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final PaymentService paymentService;

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> params) {
        long total = tradeOrderMapper.selectPageCountForMis(params);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = tradeOrderMapper.selectPageListForMis(params);
        }
        return PageResult.of(records, total);
    }

    @Override
    public int syncPaymentResult(String[] outTradeNos) {
        log.info("开始同步支付结果，待支付订单数量：{}", outTradeNos == null ? 0 : outTradeNos.length);
        if (ArrayUtil.isEmpty(outTradeNos)) {
            log.warn("订单号数组为空，无需同步支付结果");
            return 0;
        }

        int rows = 0; // 数据库表中一共更新了多少行。
        int successCount = 0; // 成功了多少个
        int failCount = 0; // 失败了多少个

        for (String outTradeNo : outTradeNos) {
            log.debug("正在查询订单支付结果，outTradeNo={}", outTradeNo);
            try {
                // 调用微信平台的接口验证是否已付款
                String transactionId = paymentService.getPaymentResult(outTradeNo);
                if (StrUtil.isNotBlank(transactionId)) {
                    // 支付成功了。更新订单状态。
                    int updateResult = tradeOrderMapper.updatePayment(outTradeNo, transactionId, 3);
                    if (updateResult > 0) {
                        rows += updateResult;
                        successCount++;
                        log.info("订单支付状态更新成功，outTradeNo={}，transactionId={}，更新行数：{}",
                                outTradeNo, transactionId, updateResult);
                    } else {
                        // 客户支付成功了，但是数据库更新失败了。
                        failCount++;
                        log.warn("订单支付状态更新失败（更新行数是0），outTradeNo={}，transactionId={}", outTradeNo, transactionId);
                    }
                } else {
                    // 这个订单还没有支付。
                    failCount++;
                    log.warn("未获取到微信支付单号（疑似未付款），outTradeNo：{}", outTradeNo);
                }
            } catch (Exception e) {
                failCount++;
                log.error("同步订单支付结果异常，outTradeNo={}", outTradeNo, e);
            }
        }

        log.info("支付结果同步完成，总数：{}，成功：{}，失败：{}， 更新记录数：{}", outTradeNos.length, successCount, failCount, rows);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Integer orderId) {
        int rows = tradeOrderMapper.deleteById(orderId);
        if (rows != 1) {
            throw new HisException("删除订单失败", 500);
        }
        log.info("删除订单，ID：{}，删除行数：{}", orderId, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processOfflineRefund(String outTradeNo) {
        int rows = tradeOrderMapper.updateStatusByOutTradeNo(outTradeNo);
        if (rows != 1) {
            throw new HisException("线下退款失败", 500);
        }
        log.info("线下退款成功，订单流水号：{}", outTradeNo);
    }
}

















