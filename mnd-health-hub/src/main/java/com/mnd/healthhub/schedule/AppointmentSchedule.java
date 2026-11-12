package com.mnd.healthhub.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import com.mnd.healthhub.db.mapper.MedAppointmentLimitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 预约相关的定时任务类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentSchedule {
    private final StringRedisTemplate redisTemplate;
    private final MedAppointmentLimitMapper medAppointmentLimitMapper;

    /**
     * 1.该定时任务在每天晚上23:55分时执行。
     * 2.执行这个任务会生成从当前时间算起第62天的限流缓存。（从次日算起就是61天的限流缓存）
     * 3.我们体检机构的系统要求只能预约从次日起60天内的。
     */
    @Scheduled(cron = "0 55 23 * * ?")
    //@Scheduled(cron = "0 23 9 * * ?")
    public void keep60d(){
        // 获取从现在起第62天的日期。
        String dateStr = DateUtil.offsetDay(new DateTime(), 61).toDateStr();

        log.info("开始生成{}的限流缓存", dateStr);

        // 获取系统默认的全局的限流规则（这个在redis当中）
        String defaultLimitStr = redisTemplate.opsForValue().get("setting#appointment_number");
        if(defaultLimitStr == null){
            log.error("系统未配置全局的限流规则，任务终止");
            return;
        }

        // 核心任务：先查那一天数据库表中有没有特殊的限流规则，没有就按照系统默认的创建，如果有则按照已存在的限流规则创建。
        // 实际的限流人数
        int actualLimit = Integer.parseInt(defaultLimitStr);
        // 实际的预约人数
        int actualCount = 0;

        // 查询数据库表当中是否存在特殊的限流规则
        Map<String, Object> rule = medAppointmentLimitMapper.selectByAppointmentDate(dateStr);
        if(rule != null){
            // 数据库表当中那一天日期是存在限流规则的，不能使用系统全局默认的。
            actualLimit = MapUtil.getInt(rule, "actualLimit");
            actualCount = MapUtil.getInt(rule, "actualCount");
            log.info("查询到特殊的限流规则：actualLimit={},actualCount={}", actualLimit, actualCount);
        }

        // 存入redis
        // key
        String key = "appointment#" + dateStr;
        // value（hash）
        Map<String, String> cache = new HashMap<>();
        cache.put("actualLimit", String.valueOf(actualLimit));
        cache.put("actualCount", String.valueOf(actualCount));
        redisTemplate.opsForHash().putAll(key, cache);

        // 设置这个缓存的有效期：1天后过期。
        redisTemplate.expireAt(key, DateUtil.offsetDay(new DateTime(dateStr), 1));

        log.info("生成{}的限流缓存：actualLimit={},actualCount={}", dateStr, actualLimit, actualCount);
    }
}






















