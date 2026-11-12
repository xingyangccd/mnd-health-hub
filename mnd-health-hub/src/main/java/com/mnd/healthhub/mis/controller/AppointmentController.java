package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.mis.controller.form.*;
import com.mnd.healthhub.mis.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("misAppointmentController")
@Slf4j
@RequestMapping("/mis/appointment")
@RequiredArgsConstructor
@Tag(name = "MIS端预约管理")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "查询预约信息列表", description = "根据订单ID查询该订单下的所有预约信息列表")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R getByOrderId(@ParameterObject @Valid GetAppointmentByOrderIdForm form) {
        List<Map<String, Object>> list = appointmentService.getByOrderId(form.getOrderId());
        return R.ok().put("result", list);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", operationId = "misAppointmentPage")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@ParameterObject @Valid PageQueryAppointmentForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = pageSize * (pageNo - 1);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = appointmentService.queryPage(param);
        return R.ok().put("pageResult", pageResult);
    }

    @DeleteMapping
    @Operation(summary = "删除预约", description = "批量删除体检预约记录（只能删除未签到的预约）")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:DELETE"}, mode = SaMode.OR)
    public R removeAppointments(@RequestBody @Valid RemoveAppointmentByIdsForm form) {
        appointmentService.removeByIds(form.getIds());
        return R.ok("删除成功");
    }

    @GetMapping("/today/status")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "核验体检人的身份")
    public R hasAppointmentInToday(@ParameterObject @Valid HasAppointmentInTodayForm form) {
        Map<String, Object> params = BeanUtil.beanToMap(form);
        int result = appointmentService.hasAppointmentInToday(params);
        return R.ok().put("result", result); // 1 -1 0
    }

    @PutMapping("/checkin")
    @Operation(summary = "签到")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:UPDATE"}, mode = SaMode.OR)
    public R checkin(@RequestBody @Valid CheckinAppointmentForm form) {
        Map<String, Object> params = BeanUtil.beanToMap(form);
        boolean result = appointmentService.checkin(params);
        return R.ok().put("result", result);
    }

    @GetMapping("/guidance-info")
    @Operation(summary = "加载导诊信息")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    public R loadGuidanceInfo(@ParameterObject @Valid LoadGuidanceInfoForm form) {
        Map<String, Object> map = appointmentService.loadGuidanceInfo(form.getId());
        return R.ok().put("result", map);
    }

    @PostMapping("/finish")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "完成体检", description = "更新预约状态为已完成，并自动处理订单状态和生成报告")
    public R finish(@RequestBody @Valid FinishExamForm form) {
        Map<String, Object> param = BeanUtil.beanToMap(form);
        boolean result = appointmentService.updateAppointmentStatus(param);
        return R.ok().put("result", result);
    }

    @GetMapping("/info")
    @SaCheckPermission(value = {"ROOT", "APPOINTMENT:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "根据预约编号查询预约信息", description = "根据预约编号获取预约详细信息")
    public R getByAppointmentNo(@ParameterObject @Valid GetAppointmentByNoForm form) {
        Map<String, Object> map = appointmentService.getByAppointmentNo(form.getAppointmentNo());
        return R.ok().put("result", map);
    }
}
