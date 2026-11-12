package com.mnd.healthhub.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.db.mapper.MedDepartmentFlowMapper;
import com.mnd.healthhub.exception.HisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlowRegulationSchedule {

    private final MedDepartmentFlowMapper medDepartmentFlowMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 刷新调流规则的定时任务
     */
    //@Scheduled(cron = "0 * 7-16 * * ?") // 体检机构要求：每天7-16点期间，每分钟执行一次。【生产环境下】
    @Scheduled(cron = "0,20,40 * * * * ?") // 测试环境中：每分钟的0秒 20秒 40秒的时候执行，每分钟执行三次。
    public void refreshFlowRegulation() {
        log.info("开始刷新调流缓存");

        // 1. 更新每个科室门口的当前排队人数（数据库层面的）
        updateDeptCurrentCount();

        // 2. 更新Redis缓存中的推荐科室列表（Redis层面的）
        refreshRecommendationCache();

        log.info("调流缓存刷新成功");
    }

    /**
     * 更新数据库：每个科室门口当前实际的排队人数需要更新。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeptCurrentCount() {
        List<Map<String, Object>> deptList = medDepartmentFlowMapper.selectAllDept();
        if (CollUtil.isEmpty(deptList)) {
            log.warn("未查询到调流科室");
            return;
        }
        // 获取redis当中所有以 flow_regulation_customer# 开头的key
        Set<String> keys = redisTemplate.keys("flow_regulation_customer#*");
        if (CollUtil.isEmpty(keys)) {
            log.warn("未查询到排队缓存，所有科室排队人数会全部重置为0");
        }

        for (Map<String, Object> dept : deptList) {
            // 在这里处理其中一个科室的数据。
            int deptId = MapUtil.getInt(dept, "id");
            String deptName = MapUtil.getStr(dept, "departmentName");

            // 统计当前这个部门 门口真实排队的人数。
            int currentCount = 0;
            for (String key : keys) {
                // 科室ID
                String value = redisTemplate.opsForValue().get(key);
                if (value != null && deptId == Integer.parseInt(value)) {
                    // 当前科室门口的人数 + 1
                    currentCount++;
                }
            }

            // 更新数据库
            Map<String, Object> map = new HashMap<>();
            map.put("id", deptId);
            map.put("currentCount", currentCount);
            int rows = medDepartmentFlowMapper.updateCurrentCount(map);
            if (rows != 1) {
                log.error("更新科室排队人数失败，科室：{}，ID：{}", deptName, deptId);
                throw new HisException("更新科室排队人数失败", 500);
            }
        }

        log.info("科室排队人数统计完成，共{}个科室", deptList.size());
    }

    /**
     * 更新redis当中的推荐列表缓存
     */
    public void refreshRecommendationCache() {
        // 系统当前采用的是什么调流模式：手动、自动
        String value = redisTemplate.opsForValue().get("setting#auto_flow_regulation");
        boolean mode = Boolean.parseBoolean(value);

        log.info("当前调流模式：{}", mode ? "自动调流" : "手动调流");

        List<Map<String, Object>> recommendations;
        if (mode) {
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithWeight();
        } else {
            recommendations = medDepartmentFlowMapper.selectRecommendationsWithPriority();
        }

        if (CollUtil.isEmpty(recommendations)) {
            log.warn("未查询到推荐科室");
            return;
        }

        List<String> result = new ArrayList<>();
        for (Map<String, Object> recommendation : recommendations) {
            result.add(JSONUtil.toJsonStr(recommendation));
        }

        redisTemplate.delete("flow_regulation");
        redisTemplate.opsForList().rightPushAll("flow_regulation", result.toArray(new String[0]));

        log.info("推荐科室列表缓存更新完成，共{}个科室", result.size());
    }


}
