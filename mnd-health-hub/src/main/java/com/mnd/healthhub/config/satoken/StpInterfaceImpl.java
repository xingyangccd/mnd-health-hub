package com.mnd.healthhub.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.convert.Convert;
import com.mnd.healthhub.db.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 这个类是sa-token框架需要的，程序员提供实现，sa-token自动调用。
 * 当API接口方法上有 @SaCheckPermission 注解时，StpInterface中的getPermissionList被调用。
 */
@Component // 纳入IoC容器的管理。
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    // 我们使用这种方式
    @Override
    public List<String> getPermissionList(Object userId, String loginType) {
        Set<String> permissions = sysUserMapper.selectPermissionsByUserId(Convert.toInt(userId));
        return new ArrayList<>(permissions);
    }

    // 咱们项目中不使用这种方式。
    @Override
    public List<String> getRoleList(Object userId, String loginType) {
        return new ArrayList<>();
    }
}
