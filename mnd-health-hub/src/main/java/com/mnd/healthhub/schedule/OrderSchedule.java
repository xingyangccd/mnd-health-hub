package com.mnd.healthhub.schedule;

import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSchedule {

    private final TradeOrderMapper tradeOrderMapper;

    // 每小时执行一次这个定时任务
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void closeOrder() {
        int rows = tradeOrderMapper.closeOrder();
        if (rows > 0) {
            log.info("关闭了{}个未支付的订单", rows);
        } else {
            log.debug("没有需要关闭的未支付的订单");
        }
    }

}
