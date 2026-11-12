package com.mnd.healthhub.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.front.controller.form.CustomerLoginForm;
import com.mnd.healthhub.front.controller.form.SendSmsCodeForm;
import com.mnd.healthhub.front.controller.form.UpdateCustomerForm;
import com.mnd.healthhub.front.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("frontCustomerController")
@RequestMapping("/front/customer")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "客户管理", description = "客户相关接口")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/sms-code")
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送短信验证码，用于登录/注册验证")
    public R sendSmsCode(@Valid @RequestBody SendSmsCodeForm form) {
        boolean success = customerService.sendSmsCode(form.getPhone());
        String msg = success ? "短信验证码发送成功" : "短信验证码发送失败";
        return R.ok(msg).put("result", success);
    }

    @PostMapping("/login")
    @Operation(summary = "手机号登录", description = "通过手机号和验证码进行登录，如果用户没有注册则自动注册")
    public R login(@Valid @RequestBody CustomerLoginForm form) {
        String phone = form.getPhone();
        String code = form.getCode();

        Map<String, Object> map = customerService.login(phone, code);

        Boolean result = MapUtil.getBool(map, "result");
        String msg = MapUtil.getStr(map, "msg");
        R r = R.ok(msg).put("result", result);

        if (result) {
            // 验证通过了。生成令牌。
            Integer id = MapUtil.getInt(map, "id");
            StpCustomerUtil.login(id, "PC");
            String token = StpCustomerUtil.getTokenValue();
            r.put("token", token);
        }
        return r;
    }

    @GetMapping("/auth/status")
    @Operation(summary = "检查用户登录状态")
    public R checkLogin() {
        // 使用StpCustomerUtil，不要使用StpUtil
        boolean isLogin = StpCustomerUtil.isLogin();
        return R.ok().put("result", isLogin);
    }

    @DeleteMapping("/auth/session")
    // 特别注意：必须指定type（业务端和mis端使用的是多账号认证方式）
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "客户登出", description = "客户退出当前登录状态，销毁服务器端session和客户端token")
    public R logout() {
        int loginId = StpCustomerUtil.getLoginIdAsInt();
        StpCustomerUtil.logout(loginId, "PC");
        return R.ok();
    }

    @GetMapping("/summary")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "获取客户信息摘要", description = "根据当前登录的客户ID获取摘要信息")
    public R getSummary() {
        int id = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> map = customerService.getSummaryById(id);
        return R.ok().put("result", map);
    }

    @PutMapping
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "修改客户信息", description = "更新当前登录用户的基本信息（姓名、性别、手机号）")
    public R update(@RequestBody @Valid UpdateCustomerForm form) {
        int id = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("id", id);
        customerService.update(param);
        return R.ok("更新成功");
    }
}
