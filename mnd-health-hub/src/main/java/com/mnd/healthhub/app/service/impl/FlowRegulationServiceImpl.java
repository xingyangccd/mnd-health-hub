package com.mnd.healthhub.app.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.app.service.FlowRegulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("appFlowRegulationService")
@Slf4j
@RequiredArgsConstructor
public class FlowRegulationServiceImpl implements FlowRegulationService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public List<JSONObject> getRecommendations() {
        List<String> list = redisTemplate.opsForList().range("flow_regulation", 0, -1);
        if (CollUtil.isEmpty(list)) {
            log.warn("调流缓存为空");
            return new ArrayList<>();
        }

        List<JSONObject> result = new ArrayList<>(list.size());
        for (String one : list) {
            JSONObject json = JSONUtil.parseObj(one);
            result.add(json);
        }
        log.info("获取调流推荐列表成功，共{}个科室", result.size());
        return result;
    }

}
