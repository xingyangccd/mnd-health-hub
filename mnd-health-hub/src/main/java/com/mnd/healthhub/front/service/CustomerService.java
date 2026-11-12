package com.mnd.healthhub.front.service;

import java.util.Map;

public interface CustomerService {
    /**
     * 发送手机短信验证码
     *
     * @param phone 手机号
     * @return true表示发送验证码成功，false表示发送验证码失败
     */
    boolean sendSmsCode(String phone);

    /**
     * 客户登录/注册
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 登录/注册的结果
     */
    Map<String, Object> login(String phone, String code);

    /**
     * 根据客户的id获取客户的摘要信息
     *
     * @param id 客户id
     * @return 客户的摘要信息
     */
    Map<String, Object> getSummaryById(Integer id);

    /**
     * 更新客户信息
     *
     * @param param 客户信息
     */
    void update(Map<String, Object> param);
}
