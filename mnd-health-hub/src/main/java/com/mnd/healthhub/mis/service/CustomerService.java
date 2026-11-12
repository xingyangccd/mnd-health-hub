package com.mnd.healthhub.mis.service;

import java.util.Map;

public interface CustomerService {
    /**
     * 获取客户的摘要信息
     *
     * @param customerId 客户的ID
     * @return 摘要信息
     */
    Map<String, Object> getSummaryById(Integer customerId);
}
