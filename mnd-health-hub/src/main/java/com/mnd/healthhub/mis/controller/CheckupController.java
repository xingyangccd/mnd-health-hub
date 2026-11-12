package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.controller.form.AddCheckupResultForm;
import com.mnd.healthhub.mis.controller.form.GetCheckupForm;
import com.mnd.healthhub.mis.service.CheckupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mis/checkup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "体检项目管理", description = "体检项目相关接口")
public class CheckupController {

    private final CheckupService checkupService;

    @GetMapping
    @Operation(summary = "查询体检项目", description = "根据预约编号和体检科室查询对应的体检项目列表")
    @SaCheckPermission(value = {"ROOT", "CHECKUP:SELECT"}, mode = SaMode.OR)
    public R listCheckup(@Valid @ParameterObject GetCheckupForm form) {
        Map<String, Object> map = checkupService.getCheckupByPlace(form.getAppointmentNo(), form.getPlace());
        return R.ok().put("result", map);
    }

    @PostMapping("/result")
    @Operation(summary = "添加或更新体检结果", description = "根据预约编号和科室添加或更新体检结果")
    @SaCheckPermission(value = {"ROOT", "CHECKUP:INSERT", "CHECKUP:UPDATE"}, mode = SaMode.OR)
    public R addResult(@RequestBody @Valid AddCheckupResultForm form) {
        int doctorId = StpUtil.getLoginIdAsInt();
        checkupService.addResult(doctorId, form.getDoctorName(), form.getAppointmentNo(),
                form.getPlace(), form.getTemplate(), form.getItem());
        return R.ok("保存体检结果成功");
    }
}