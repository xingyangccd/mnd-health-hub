package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.service.CustomerImService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("misCustomerImController")
@RequestMapping("/mis/customer/im")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "MIS端客服IM管理")
public class CustomerImController {
    private final CustomerImService customerImService;

    @GetMapping("/account")
    @SaCheckPermission(value = {"ROOT", "CUSTOMER_IM:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "为客服专员颁发登录凭证")
    public R getServiceAccount() {
        Map<String, Object> map = customerImService.getServiceAccount();
        return R.ok().put("result", map);
    }
}
