package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.db.entity.SysUserEntity;
import com.mnd.healthhub.mis.controller.form.*;
import com.mnd.healthhub.mis.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController("misUserController")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/mis/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class SysUserController {
    private final SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回token和权限列表")
    public R login(@RequestBody @Valid LoginForm form) {
        Integer userId = sysUserService.login(form.getUsername(), form.getPassword());
        // sa-token颁发令牌(底层会自动生成token，然后将token+userId存储到redis当中....)
        StpUtil.login(userId, "WEB");
        // 获取令牌
        String token = StpUtil.getTokenValue();
        // 获取权限列表（sa-token可以给你权限列表）
        List<String> permissions = StpUtil.getPermissionList();
        // 返回R对象
        return R.ok().put("token", token).put("permissions", permissions);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出系统销毁令牌")
    @SaCheckLogin // 只有登录的用户才能登出（sa-token拦截，获取前端系统提交的令牌 token，然后去redis中验证token，验证通过之后，web接口中的方法体就会执行。）
    public R logout() {
        // sa-token当中的api：通过token获取redis中存储的userId
        int userId = StpUtil.getLoginIdAsInt();
        // 退出系统（只是WEB端账号退出，其他端不会退出）
        StpUtil.logout(userId, "WEB");
        return R.ok("登出成功");
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @SaCheckLogin // 检查是否登录了。
    public R updatePassword(@RequestBody @Valid UpdatePasswordForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        sysUserService.updatePassword(userId, form.getOldPassword(), form.getNewPassword());
        // 修改密码之后需要退出登录（重新登录）
        StpUtil.logout(userId, "WEB");
        return R.ok("密码修改成功");
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "根据查询条件分页查询用户信息")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R list(@ParameterObject @Valid UserPageQueryForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        // 封装查询条件的Map集合
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = sysUserService.queryPage(params);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping
    @Operation(summary = "保存用户", description = "保存用户信息")
    @SaCheckPermission(value = {"ROOT", "USER:INSERT"}, mode = SaMode.OR)
    public R save(@RequestBody @Valid SaveUserForm form) {
        // 将form转换成SysUserEntity对象
        SysUserEntity user = BeanUtil.toBean(form, SysUserEntity.class);
        // 底层数据库role_ids是一个json字符串类型。所以user对象当中的roleIds是一个String类型
        // form中的roleIds属性是Integer[]。
        // BeanUtil.toBean()这个方法并不会自动将 Integer[] ==> String
        // 这个转换需要手动转换。
        user.setRoleIds(JSONUtil.toJsonStr(form.getRoleIds())); // hutool包下的工具方法
        sysUserService.save(user);
        return R.ok("用户信息保存成功");
    }

    // 路径参数：因为参数只有1个，不使用form了。
    @GetMapping("/{userId}")
    @Operation(summary = "根据ID查询用户信息")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R getById(
            @PathVariable // SpringMVC注解
            @Parameter(description = "用户ID", required = true, example = "1") // Swagger注解
            @NotNull(message = "userId不能为空") // 校验框架的注解
            @Min(value = 1, message = "userId不能小于1") // 校验框架的注解
            Integer userId) {
        Map<String, Object> user = sysUserService.getById(userId);
        return R.ok().put("user", user);
    }

    @PutMapping
    @Operation(summary = "修改用户信息")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    public R update(@RequestBody @Valid UpdateUserForm form) {
        Map<String, Object> user = BeanUtil.beanToMap(form);
        user.replace("roleIds", JSONUtil.toJsonStr(form.getRoleIds()));
        sysUserService.update(user);
        // 更新用户之后，一定要将用户踢下线（只有重新登录才能获取到最新的角色和权限）
        StpUtil.logout(form.getUserId()); // 所有设备踢下线
        return R.ok("用户信息修改成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户", description = "根据用户ID批量删除用户信息，并强制踢下线")
    @SaCheckPermission(value = {"ROOT", "USER:DELETE"}, mode = SaMode.OR)
    public R batchDelete(@RequestBody @Valid RemoveUserByIdsForm form) {
        // 当前登录的用户
        int currentUserId = StpUtil.getLoginIdAsInt();
        // 要删除用户id集合
        Integer[] userIds = form.getUserIds();
        // 判断集合中是否包含当前登录的用户
        if (ArrayUtil.contains(userIds, currentUserId)) {
            return R.error("您不能删除自己");
        }
        // 执行删除
        int rows = sysUserService.removeByIds(userIds);
        if (rows > 0) {
            // Stream API
            // 循环将用户踢下线
            Arrays.stream(userIds).forEach(StpUtil::logout); // StpUtil::logout 方法引用
        }
        // 返回删除的记录条数。
        return R.ok().put("rows", rows);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "员工离职", description = "为用户ID办理离职并强制退出登录")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    public R dismiss(
            @PathVariable
            @Parameter(description = "用户ID", required = true, example = "1")
            @NotNull(message = "userId不能为空")
            @Min(value = 1, message = "userId不能小于1")
            Integer userId) {
        sysUserService.dismiss(userId);
        StpUtil.logout(userId);
        return R.ok("离职成功");
    }

    @GetMapping("/doctor/profile")
    @SaCheckLogin
    @Operation(summary = "查询当前医生信息")
    public R getDoctorProfile() {
        int doctorId = StpUtil.getLoginIdAsInt();
        Map<String, Object> map = sysUserService.getDoctorById(doctorId);
        return R.ok().put("result", map);
    }

}
