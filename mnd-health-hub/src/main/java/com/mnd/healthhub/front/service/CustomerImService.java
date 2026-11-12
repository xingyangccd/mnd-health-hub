package com.mnd.healthhub.front.service;

import java.util.Map;

public interface CustomerImService {
    /**
     * 创建IM账户（给客户颁发登录凭证）
     *
     * @param customerId 客户id
     * @return IM账户登录凭证。
     */
    Map<String, Object> createAccount(Integer customerId);
}
