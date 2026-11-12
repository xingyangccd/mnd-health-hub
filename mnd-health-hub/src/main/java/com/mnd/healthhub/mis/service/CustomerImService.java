package com.mnd.healthhub.mis.service;

import java.util.Map;

public interface CustomerImService {
    /**
     * 给大健康客服专员颁发登录凭证。
     *
     * @return 客服专员的登录凭证
     */
    Map<String, Object> getServiceAccount();
}
