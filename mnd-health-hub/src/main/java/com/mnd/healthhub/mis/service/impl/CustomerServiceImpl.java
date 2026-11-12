package com.mnd.healthhub.mis.service.impl;

import com.mnd.healthhub.db.mapper.CrmCustomerMapper;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("misCustomerService")
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final TradeOrderMapper orderMapper;
    private final CrmCustomerMapper customerMapper;

    @Override
    public Map<String, Object> getSummaryById(Integer customerId) {
        Map<String, Object> map = customerMapper.selectById(customerId);
        if (map == null) {
            throw new HisException("客户信息不存在", 400);
        }
        map.putAll(orderMapper.selectOrderStatistics(customerId));
        return map;
    }
}
