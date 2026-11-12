package com.mnd.healthhub.async;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.db.entity.SysConfigEntity;
import com.mnd.healthhub.db.mapper.MedAppointmentLimitMapper;
import com.mnd.healthhub.db.mapper.MedDepartmentFlowMapper;
import com.mnd.healthhub.db.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 核心：在服务器启动阶段，异步执行初始化工作。初始化Redis缓存。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncInitWork {
    private final SysConfigMapper sysConfigMapper;
    private final MedAppointmentLimitMapper medAppointmentLimitMapper;
    private final StringRedisTemplate redisTemplate;
    private final MedDepartmentFlowMapper medDepartmentFlowMapper;

    // 异步任务，将任务提交给线程池，线程池会自动查找空闲线程来处理这个任务。
    @Async("taskExecutor")
    public void initWork() {
        log.info("开始异步初始化任务");
        long startTime = System.currentTimeMillis();

        // 1. 加载系统全局配置
        loadSystemConfigs();

        // 2. 加载限流规则
        loadAppointmentLimitRules();

        // 3. 加载推荐列表
        updateFlowRegulationCache();

        long endTime = System.currentTimeMillis();
        log.info("异步初始化任务完成，耗时{}毫秒", endTime - startTime);
    }

    /**
     * 从限流表中查询限流规则，加载到Redis缓存当中。
     * 如果限流表中没有对应日期的限流规则，会按照系统全局配置进行加载。
     */
    private void loadAppointmentLimitRules() {
        log.info("开始加载限流规则到Redis缓存");

        // 开始日期
        DateTime startDate = DateUtil.tomorrow();
        // 结束日期
        // 基于指定的日期再加59天，最后的结果是60天。
        DateTime endDate = startDate.offsetNew(DateField.DAY_OF_MONTH, 59);

        log.info("日期范围：{} 至 {}", DateUtil.formatDate(startDate), DateUtil.formatDate(endDate));

        // 查询现流表中的限流规则。
        List<Map<String, Object>> dbRules = medAppointmentLimitMapper.selectLimitRulesByDateRange(DateUtil.formatDate(startDate), DateUtil.formatDate(endDate));
        log.info("从数据库查询到限流规则，共{}条", dbRules.size());


        /*
            假设数据库中存在限流规则，将上面的List集合转换为Map集合。
            List集合是什么结构？
                1. List集合中每个元素是 Map<String, Object>
                    第一个Map：
                        appointmentDate： 2000-10-10
                        actualLimit：     200
                        maxLimit：        200
                        actualCount：     180
                    第二个Map：
                        appointmentDate： 2000-10-11
                        actualLimit：     200
                        maxLimit：        200
                        actualCount：     170
            将以上的List集合转换为Map集合
                1. Map集合中的每个元素
                    key                     value
                    ---------------------------------
                    "2000-10-10"            第一个Map
                    "2000-10-11"            第二个Map
         */
        Map<String, Map<String, Object>> ruleMap = new HashMap<>();
        if (CollUtil.isNotEmpty(dbRules)) {
            for (Map<String, Object> dbRule : dbRules) {
                ruleMap.put(MapUtil.getStr(dbRule, "appointmentDate"), dbRule);
            }
        }

        // 获取系统全局默认的限流人数。
        String defaultLimitStr = redisTemplate.opsForValue().get("setting#appointment_number");
        int defaultLimit = 0;
        if (StrUtil.isNotBlank(defaultLimitStr)) {
            defaultLimit = Integer.parseInt(defaultLimitStr);
        }

        log.info("系统全局默认配置的限流人数：{}", defaultLimit);

        // 获取60个日期对象
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);

        int count = 0;
        for (DateTime date : range) {
            // 获取限流规则（获取数据库当中的限流规则）
            String dateStr = DateUtil.formatDate(date);
            Map<String, Object> rule = ruleMap.get(dateStr);

            int actualLimit = defaultLimit;
            int actualCount = 0;
            if (rule != null) {
                // 条件成立说明这个预约日期下数据库表中存在限流规则
                actualLimit = MapUtil.getInt(rule, "actualLimit");
                actualCount = MapUtil.getInt(rule, "actualCount");
            }

            // 向Redis缓存中保存数据，需要准备key
            String key = "appointment#" + dateStr;
            Map<String, String> cache = new HashMap<>();
            cache.put("actualLimit", String.valueOf(actualLimit));
            cache.put("actualCount", String.valueOf(actualCount));
            redisTemplate.opsForHash().putAll(key, cache);

            // 设置key的过期时间
            redisTemplate.expireAt(key, DateUtil.beginOfDay(date).offsetNew(DateField.DAY_OF_MONTH, 1));
            count++;
        }

        log.info("限流规则已加载到Redis缓存，共{}条", count);

    }

    /**
     * 加载系统全局配置到Redis当中。
     */
    private void loadSystemConfigs() {
        log.info("开始加载系统配置到Redis");
        List<SysConfigEntity> sysConfigEntities = sysConfigMapper.selectAll();
        if (CollUtil.isEmpty(sysConfigEntities)) {
            log.warn("系统配置表无任何配置");
            return;
        }
        // 存在系统配置
        for (SysConfigEntity sysConfig : sysConfigEntities) {
            redisTemplate.opsForValue().set("setting#" + sysConfig.getConfigKey(), sysConfig.getConfigValue());
        }
        log.info("系统配置已加载到Redis，共{}条配置", sysConfigEntities.size());
    }

    /**
     * 更新科室推荐列表缓存
     */
    private void updateFlowRegulationCache() {
        // 将所有科室门口的当前人数统一更新为0（数据库更新为0）
        int updateCount = medDepartmentFlowMapper.updateCurrentCount(Map.of("currentCount", 0));
        log.info("重置所有科室排队人数为0，共{}科室", updateCount);

        // 获取系统调流规则状态（手动调流/自动调流）
        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation");
        boolean mode = Boolean.parseBoolean(value);
        log.info("当前调流模式：{}", mode ? "自动调流（按权重）" : "手动调流（按优先级）");

        List<Map<String, Object>> recommendations;
        if (mode) {
            // 自动调流
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithWeight();
        } else {
            // 手动调流
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithPriority();
        }

        // 将推荐列表存储到redis当中。
        List<String> result = new ArrayList<>();
        for (Map<String, Object> recommendation : recommendations) {
            result.add(JSONUtil.toJsonStr(recommendation));
        }

        // 更新缓存
        redisTemplate.delete("flow_regulation");
        redisTemplate.opsForList().rightPushAll("flow_regulation", result.toArray(new String[0]));

        log.info("科室调流缓存更新完成，共{}个科室", result.size());
    }
}














