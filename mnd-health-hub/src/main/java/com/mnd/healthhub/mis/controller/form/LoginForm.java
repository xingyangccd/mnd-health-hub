package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录的form，专门接收前端系统提交的数据，相当于DTO。需要编写：校验规则 + swagger注解。
 */
@Data
@Schema(description = "登录请求参数form")
public class LoginForm {
    @Schema(description = "用户名（5-50位的字母或数字）", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,50}$", message = "用户名格式错误：仅支持字母和数字，长度5-50位")
    private String username;

    @Schema(description = "密码（6-20位字母或数字）", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123456")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "密码格式错误：仅支持字母和数字，长度6-20位")
    private String password;
}
