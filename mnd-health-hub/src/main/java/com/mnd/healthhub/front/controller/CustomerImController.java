package com.mnd.healthhub.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.front.service.CustomerImService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("frontCustomerImController")
@RequestMapping("/front/customer/im")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "客户IM管理")
public class CustomerImController {

    private final CustomerImService customerImService;

    @GetMapping("/account")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "创建或获取客户IM账号",
            description = "为当前登录客户创建IM账号，如果已存在则直接返回账号信息，同时返回登录凭证")
    public R createAccount(){
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        // 返回了IM账号登录凭证。
        Map<String, Object> result = customerImService.createAccount(customerId);
        return R.ok().put("result", result);
    }
}
