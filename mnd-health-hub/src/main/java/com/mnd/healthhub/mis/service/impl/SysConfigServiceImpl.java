package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.db.mapper.SysConfigMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service("misSysConfigService")
@Slf4j
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final StringRedisTemplate redisTemplate;
    private final SysConfigMapper sysConfigMapper;

    @Override
    public String getConfigValue(String configKey) {
        String configValue = redisTemplate.opsForValue().get("setting#" + configKey);
        if (StrUtil.isBlank(configValue)) {
            throw new HisException("系统中没有缓存该配置");
        }
        return configValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setConfigValue(String configKey, String configValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("configKey", configKey);
        params.put("configValue", configValue);
        int rows = sysConfigMapper.updateByConfigKey(params);
        if (rows > 0) {
            // 表示数据库更新成功了，要更新redis的缓存
            redisTemplate.opsForValue().set("setting#" + configKey, configValue);
            return true;
        }
        return false;
    }
}
