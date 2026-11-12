package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import com.mnd.healthhub.db.entity.CrmCustomerEntity;
import com.mnd.healthhub.db.mapper.CrmCustomerMapper;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    // key和value都是string类型。（操作redis的）
    private final StringRedisTemplate redisTemplate;
    private final CrmCustomerMapper crmCustomerMapper;
    private final TradeOrderMapper tradeOrderMapper;

    @Override
    public boolean sendSmsCode(String phone) {
        // 生成验证码
        String code = RandomUtil.randomNumbers(6);
        System.out.println("手机验证码：" + code);

        if (redisTemplate.hasKey("sms_code_refresh_" + phone)) {
            // 条件成立的话，说明这个手机号在1分钟之内已经发送过验证码了。
            return false;
        }

        // 符合发送手机验证码的条件
        // 存储key：sms_code_refresh_phone
        redisTemplate.opsForValue().set("sms_code_refresh_" + phone, code);
        // 设置key：sms_code_refresh_phone的有效期1分钟。
        redisTemplate.expire("sms_code_refresh_" + phone, 1, TimeUnit.MINUTES);

        // 将验证码存储到redis当中
        redisTemplate.opsForValue().set("sms_code_" + phone, code);
        // 设置验证码的有效期5分钟
        redisTemplate.expire("sms_code_" + phone, 5, TimeUnit.MINUTES);

        // 调用运营商的发送短信的API接口，完成短信的发送。
        log.info("调用运营商的发送短信的API接口:短信验证码{}发送到{}成功", code, phone);

        return true;
    }

    @Override
    public Map<String, Object> login(String phone, String code) {
        // 这是最终要返回的结果
        Map<String, Object> map = new HashMap<>();

        // 获取两个key
        String key = "sms_code_" + phone;
        String refreshKey = "sms_code_refresh_" + phone;

        // 判断验证码是否过期了。
        if (!redisTemplate.hasKey(key)) {
            // 验证码过期了
            map.put("result", false);
            map.put("msg", "短信验证码已过期");
            return map;
        }

        // 验证码没有过期（验证验证码是否有效。）
        String smsCode = redisTemplate.opsForValue().get(key).toString();
        if (!smsCode.equals(code)) {
            map.put("result", false);
            map.put("msg", "验证码错误");
            return map;
        }

        // 验证码有效（删除两个key）
        redisTemplate.delete(key);
        redisTemplate.delete(refreshKey);

        // 查询或注册用户
        Integer id = crmCustomerMapper.findIdByPhone(phone);
        if (id == null) {
            // 用户注册
            CrmCustomerEntity customer = new CrmCustomerEntity();
            customer.setPhone(phone);
            int rows = crmCustomerMapper.insert(customer);
            if (rows != 1) {
                // 保存失败了
                map.put("result", false);
                map.put("msg", "注册失败，稍后再试");
                return map;
            }
            // 怎么能够获取到新增的用户的id呢？
            id = customer.getId();
        }

        map.put("result", true);
        map.put("id", id);
        map.put("msg", "登录成功");
        return map;
    }

    @Override
    public Map<String, Object> getSummaryById(Integer id) {
        Map<String, Object> map = crmCustomerMapper.selectById(id);
        if (map == null) {
            throw new HisException("客户不存在", 404);
        }
        // 合并为一个map
        map.putAll(tradeOrderMapper.selectOrderStatistics(id));
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Map<String, Object> param) {
        int rows = crmCustomerMapper.update(param);
        if (rows != 1) {
            throw new HisException("客户信息更新失败", 500);
        }
        log.info("客户信息更新成功，id={}", MapUtil.getInt(param, "id"));
    }
}


















