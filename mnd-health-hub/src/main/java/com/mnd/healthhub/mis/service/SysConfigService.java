package com.mnd.healthhub.mis.service;

public interface SysConfigService {
    /**
     * 通过配置key获取配置value（全局配置，服务器启动时，会自动存储到redis当中）
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据key更新value
     *
     * @param configKey 配置键
     * @param value     配置值
     * @return true表示更新成功，false表示更新失败
     */
    boolean setConfigValue(String configKey, String value);
}
