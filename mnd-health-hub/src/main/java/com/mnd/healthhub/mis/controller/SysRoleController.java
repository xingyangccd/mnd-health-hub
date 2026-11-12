package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController("misRoleController")
@RequestMapping("/mis/role")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {
    private final SysRoleService sysRoleService;

    @GetMapping
    @Operation(summary = "查询所有角色", operationId = "roleList")
    @SaCheckPermission(value = {"ROOT", "ROLE:SELECT"}, mode = SaMode.OR)
    public R list(){
        List<Map<String, Object>> roleList = sysRoleService.listAll();
        return R.ok().put("roleList", roleList);
    }

}
