package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "新增用户请求参数")
public class SaveUserForm {

    @Schema(description = "用户名（5-20位字母或数字）", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin123")
    @NotBlank(message = "username不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "username内容不正确")
    private String username;

    @Schema(description = "密码（6-20位字母或数字）", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "password不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "password内容不正确")
    private String password;

    @Schema(description = "真实姓名（2-10位中文）", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "姓名内容不正确")
    private String realName;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"男", "女"}, example = "男")
    @NotBlank(message = "gender不能为空")
    @Pattern(regexp = "^男$|^女$", message = "gender内容不正确")
    private String gender;

    @Schema(description = "手机号（11位，以1开头）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "mobile不能为空")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "mobile内容不正确")
    private String mobile;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan@example.com")
    @NotBlank(message = "email内容不正确")
    @Email(message = "email内容不正确")
    private String email;

    @Schema(description = "部门ID", example = "1")
    @Min(value = 1, message = "deptId不能小于1")
    private Integer deptId;

    @Schema(description = "入职日期（格式：YYYY-MM-DD）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
    @NotBlank(message = "hireDate不能为空")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "hireDate内容不正确")
    private String hireDate;

    @Schema(description = "角色ID列表（至少选择一个）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2]")
    @NotEmpty(message = "roleIds不能为空")
    private Integer[] roleIds;
}