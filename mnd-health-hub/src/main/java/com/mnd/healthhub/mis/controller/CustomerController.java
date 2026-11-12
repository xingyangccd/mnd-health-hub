package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.controller.form.GetCustomerSummaryForm;
import com.mnd.healthhub.mis.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("misCustomerController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mis/customer")
@Tag(name = "MIS端客户管理")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/summary")
    @Operation(summary = "查询客户摘要信息")
    @SaCheckPermission(value = {"ROOT", "CUSTOMER:SELECT", "ORDER:SELECT"}, mode = SaMode.OR)
    public R summary(@ParameterObject @Valid GetCustomerSummaryForm form) {
        Map<String, Object> result = customerService.getSummaryById(form.getCustomerId());
        return R.ok().put("result", result);
    }
}
