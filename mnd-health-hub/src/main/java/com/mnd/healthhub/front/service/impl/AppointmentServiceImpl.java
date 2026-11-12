package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.map.MapUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedExamAppointmentEntity;
import com.mnd.healthhub.db.mapper.MedAppointmentLimitMapper;
import com.mnd.healthhub.db.mapper.MedExamAppointmentMapper;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service("frontAppointmentService")
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    // 数据库相关的，包括redis，mysql等。
    private final StringRedisTemplate stringRedisTemplate;
    private final MedExamAppointmentMapper medExamAppointmentMapper;
    private final MedAppointmentLimitMapper medAppointmentLimitMapper;
    private final TradeOrderMapper tradeOrderMapper;

    // 返回值是字符串，我定义为常量了。
    private static final String SUCCESS = "预约成功";
    private static final String FULL = "当天预约已满，请选择其它日期";
    private static final String FAIL = "预约失败";

    // Redis Key 前缀，完整格式：appointment#{预约日期}
    private static final String REDIS_KEY_PREFIX = "appointment#";
    // 订单状态码：已预约
    private static final int ORDER_STATUS_APPOINTED = 5;
    // Redis 回滚最大重试次数（应对网络抖动等临时故障）
    private static final int ROLLBACK_MAX_RETRIES = 3;
    // Redis 回滚重试间隔时间（毫秒）
    private static final long ROLLBACK_RETRY_INTERVAL_MS = 100L;

    /**
     * Lua 脚本：原子性地检查并扣减库存
     * KEYS[1] = Redis key，例如 "appointment#2026-09-01"
     * ARGV[1] = 扣减数量，固定为 1
     * <p>
     * 返回值：
     * 1 = 扣减成功（还有余量）
     * 0 = 库存已满（actualCount >= actualLimit）
     * -1 = key 不存在或数据异常
     */
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local decrement = tonumber(ARGV[1]) or 1
            local actualCount = redis.call('hget', key, 'actualCount')
            local actualLimit = redis.call('hget', key, 'actualLimit')
            if actualCount == false or actualLimit == false then
                return -1
            end
            actualCount = tonumber(actualCount)
            actualLimit = tonumber(actualLimit)
            if actualCount < actualLimit then
                redis.call('hincrby', key, 'actualCount', decrement)
                return 1
            else
                return 0
            end
            """;

    /**
     * Lua 脚本：回滚 Redis 中的 actualCount（减回去）
     * <p>
     * 为什么是减回去？
     * 预约流程：先扣 Redis（actualCount + 1），再写数据库。
     * 如果数据库失败，需要把之前加的 1 减回去（actualCount - 1），
     * 让 Redis 和数据库保持一致。
     * <p>
     * KEYS[1] = Redis key
     * ARGV[1] = 回滚数量，固定为 1
     * 返回值：固定返回 1
     */
    private static final String ROLLBACK_SCRIPT = """
            local key = KEYS[1]
            local decrement = tonumber(ARGV[1]) or 1
            redis.call('hincrby', key, 'actualCount', -decrement)
            return 1
            """;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(MedExamAppointmentEntity appointment) {
        // 获取预约日期
        String appointmentDate = appointment.getAppointmentDate();
        // 拼接redis中的key
        String key = REDIS_KEY_PREFIX + appointmentDate;

        log.info("开始处理预约，预约日期：{}，订单号：{}", appointmentDate, appointment.getOrderId());

        // 1.执行Lua脚本扣减Redis库存（原子性操作）
        Long result = executeDeductScript(key);
        if(result == null || result == -1L){
            log.error("Redis缓存异常，key:{}，预约日期：{}", key, appointmentDate);
            return FAIL;
        }else if(result == 0L) {
            log.warn("预约已满，预约日期：{}，订单号：{}", appointmentDate, appointment.getOrderId());
            return FULL;
        }

        log.info("Redis扣减成功，预约日期：{}，订单号：{}", appointmentDate, appointment.getOrderId());

        try {
            // 2.执行数据库操作
            saveAppointmentToDB(appointment, key);
            // 数据库的操作成功之后，才能走到这里。
            log.info("预约成功，预约日期：{}，订单号：{}", appointmentDate, appointment.getOrderId());
            return SUCCESS;
        }catch (Exception e){
            log.error("数据库操作失败，开始回滚Redis，预约日期：{}，订单号：{}，错误：{}", appointmentDate, appointment.getOrderId(), e.getMessage(), e);
            // 3.数据库执行失败，需要回滚Redis库存。
            rollbackRedis(key);
            throw new HisException("预约失败：" + e.getMessage(), e);
        }
    }

    /**
     * 回滚Redis
     * @param key 回滚的键
     */
    private void rollbackRedis(String key) {
        log.info("开始回滚Redis，key：{}", key);

        // 创建Lua脚本对象
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(ROLLBACK_SCRIPT);
        script.setResultType(Long.class);

        // 三次机会。
        for (int i = 1; i <= ROLLBACK_MAX_RETRIES; i++) {
            try {
                Long result = redisTemplate.execute(script, Collections.singletonList(key), "1");
                if(result == 1L){
                    log.info("Redis回滚成功，key：{}，重试次数：{}", key, i);
                    // 直接结束了
                    return;
                }
                log.warn("Redis回滚返回异常，key：{}，result：{}，重试次数：{}", key, result, i);
            }catch (Exception e){
                log.error("Redis回滚执行异常，key：{}，重试次数：{}", key, i, e);
            }

            // 控制每一次重试的时间间隔（每隔0.1秒/100ms重试一次）
            if(i < ROLLBACK_MAX_RETRIES){ // 最后一次尝试失败之后，不需要再进行100ms的时间间隔（避免浪费资源）
                try {
                    TimeUnit.MILLISECONDS.sleep(ROLLBACK_RETRY_INTERVAL_MS);
                } catch (InterruptedException e) {
                    // 中断当前线程的sleep
                    Thread.currentThread().interrupt();
                    log.warn("回滚重试被中断，key：{}", key);
                    break;
                }
            }
        }

        // 程序尝试了3次都失败了。才能走到这里。记录日志（给相关人员发送短信/打电话/人工补偿）
        log.error("Redis回滚最终失败，key：{}，时间：{}，需要人工处理",
                key, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // TODO: 写入补偿表或告警
    }

    /**
     * 完成预约业务流程中数据库的操作
     * @param appointment 预约对象
     * @param key 预约key
     */
    private void saveAppointmentToDB(MedExamAppointmentEntity appointment, String key) {
        // 1. 插入预约记录
        log.info("开始插入预约记录，订单号：{}", appointment.getOrderId());
        int insertRows = medExamAppointmentMapper.insert(appointment);
        if(insertRows != 1){
            throw new HisException("保存预约记录失败，影响行数：" + insertRows);
        }

        // 2. 从Redis中取出当前实际的限流人数: actualLimit
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
        Integer actualLimit = MapUtil.getInt(hash, "actualLimit");

        // 3. 更新/插入限流表
        log.info("开始更新限流表，预约日期：{}，实际限流人数：{}", appointment.getAppointmentDate(), actualLimit);
        Map<String, Object> limitMap = new HashMap<>();
        limitMap.put("appointmentDate", appointment.getAppointmentDate());
        limitMap.put("actualLimit", actualLimit);
        limitMap.put("maxLimit", actualLimit);
        limitMap.put("remark", "预约限流");
        int limitRows = medAppointmentLimitMapper.insertOrUpdateAppointmentLimit(limitMap);
        if(limitRows == 0){
            throw new HisException("更新/保存限流规则失败");
        }

        // 4. 更新订单状态
        log.info("开始更新订单状态，订单号：{}，状态：{}", appointment.getOrderId(), ORDER_STATUS_APPOINTED);
        int updateRows = tradeOrderMapper.updateStatus(appointment.getOrderId(), ORDER_STATUS_APPOINTED);
        if(updateRows != 1){
            throw new HisException("更新订单状态失败，影响行数：" + updateRows);
        }
    }

    /**
     * 执行扣减库存的Lua脚本
     * @param key redis的key
     * @return 1、-1、0
     */
    private Long executeDeductScript(String key) {
        try {
            // 封装Lua脚本对象
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LUA_SCRIPT); // 执行哪个lua脚本
            script.setResultType(Long.class); // 返回值的类型

            // 封装key的List集合
            List<String> keys = Collections.singletonList(key);
            // 封装数据的List集合
            List<String> args = Collections.singletonList("1");

            // 执行lua脚本
            return redisTemplate.execute(script, keys, args.toArray());
        }catch (Exception e){
            log.error("执行Redis Lua脚本扣减库存时异常，key：{}", key, e);
            return null;
        }
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> param) {
        long total = medExamAppointmentMapper.selectPageCount(param);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = medExamAppointmentMapper.selectPageList(param);
        }
        return PageResult.of(records, total);
    }
}


















