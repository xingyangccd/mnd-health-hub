package com.mnd.healthhub.mis.service.impl;

import com.mnd.healthhub.mis.service.CustomerImService;
import com.tencentyun.TLSSigAPIv2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("misCustomerImService")
@Slf4j
public class CustomerImServiceImpl implements CustomerImService {

    @Value("${tencent.im.sdkAppId}")
    private Long sdkAppId;

    @Value("${tencent.im.secretKey}")
    private String secretKey;

    @Value("${tencent.im.customerServiceId}")
    private String customerServiceId;

    private static final int USER_SIG_EXPIRE = 180 * 86400;

    /**
     * 给客服专员颁发登录凭证。
     *
     * @return 客服专员的登录凭证。
     */
    @Override
    public Map<String, Object> getServiceAccount() {
        TLSSigAPIv2 api = new TLSSigAPIv2(sdkAppId, secretKey);
        String userSig = api.genUserSig(customerServiceId, USER_SIG_EXPIRE);

        // 客服的登录凭证。
        Map<String, Object> result = new HashMap<>();
        result.put("sdkAppId", sdkAppId);
        result.put("account", customerServiceId);
        result.put("userSig", userSig);

        // 返回登录凭证。
        return result;
    }
}
