package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("misDeptController")
@RequiredArgsConstructor
@RequestMapping("/mis/dept")
@Slf4j
@Tag(name = "部门管理")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @GetMapping
    @Operation(summary = "查询所有部门", operationId = "deptList")
    @SaCheckPermission(value = {"ROOT", "DEPT:SELECT"}, mode = SaMode.OR)
    public R list() {
        List<Map<String, Object>> deptList = sysDeptService.listAll();
        return R.ok().put("deptList", deptList);
    }

}
