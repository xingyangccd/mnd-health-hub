package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "用户分页查询请求参数")
public class UserPageQueryForm {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "10")
    @Range(min = 10, max = 50, message = "每页条数必须在10~50之间")
    private Integer pageSize = 10;

    // realName可以接受什么值？null、""、1-10个长度中文
    @Schema(description = "真实姓名（中文）", example = "张三")
    @Pattern(regexp = "^$|^[\\u4e00-\\u9fa5]{1,10}$", message = "realName内容不正确")
    private String realName;

    // 以下的校验规则中要求gender可以接受的三个值：null、男、女，其他不行。
    @Schema(description = "性别", allowableValues = {"男", "女"}, example = "男")
    @Pattern(regexp = "^男$|^女$", message = "性别错误：只能为'男'或'女'")
    private String gender;

    // 前端系统提交的是空字符串，后端的类型是包装类，springboot会将 "" 自动转换为 null，赋值给包装类对象。
    @Schema(description = "角色ID", example = "1")
    @Min(value = 1, message = "角色ID不能小于1")
    private Integer roleId;

    @Schema(description = "部门ID", example = "1")
    @Min(value = 1, message = "部门ID不能小于1")
    private Integer deptId;

    @Schema(description = "用户状态", allowableValues = {"1", "2"}, example = "1")
    @Range(min = 1, max = 2, message = "用户状态错误：1-正常，2-离职")
    private Integer userStatus;
}