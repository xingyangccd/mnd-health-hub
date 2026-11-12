package com.mnd.healthhub.config;

import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取被删除的 key
        String expiredKey = message.toString();
        // 记录日志
        log.info("redis的key被删除：key = {}", expiredKey);
        // 从redis key中获取订单流水号。
        if (expiredKey.startsWith("codeUrl")) {
            // 关闭订单
            String[] arr = expiredKey.split("_");
            String outTradeNo = arr[2];
            int rows = tradeOrderMapper.closeOrderByOutTradeNo(outTradeNo);
            if(rows != 1){
                log.error("关闭超时未支付的订单失败，订单流水号：{}", outTradeNo);
                throw new HisException("超时未支付关单失败");
            }
            log.info("成功关闭超时未支付的订单，订单流水号：{}", outTradeNo);
        }
    }
}