package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.db.entity.MedDepartmentFlowEntity;
import com.mnd.healthhub.mis.controller.form.*;
import com.mnd.healthhub.mis.service.FlowRegulationService;
import com.mnd.healthhub.mis.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mis/flow-regulation")
@Tag(name = "流量调控管理")
public class FlowRegulationController {
    private final FlowRegulationService flowRegulationService;
    private final SysConfigService sysConfigService;

    @GetMapping
    @Operation(summary = "获取调流规则开关状态")
    @SaCheckLogin
    public R loadFlowStatus() {
        String switchStatus = sysConfigService.getConfigValue("auto_flow_regulation");
        return R.ok().put("result", Boolean.parseBoolean(switchStatus));
    }

    @GetMapping("/dept-names")
    @Operation(summary = "加载科室名字")
    @SaCheckLogin
    public R getDeptNames() {
        List<String> deptNames = flowRegulationService.getDeptNames();
        return R.ok().put("result", deptNames);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询流量调控列表", description = "根据科室名称、信标UUID等条件分页查询流量调控配置")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:SELECT"}, mode = SaMode.OR)
    public R pageQuery(@ParameterObject @Valid PageQueryFlowRegulationForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = pageSize * (pageNo - 1);
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        PageResult<MedDepartmentFlowEntity> pageResult = flowRegulationService.queryPage(param);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping
    @Operation(summary = "新增流量调控配置", description = "新增科室流量调控配置")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:INSERT"}, mode = SaMode.OR)
    public R addFlowRegulation(@RequestBody @Valid SaveFlowRegulationForm form) {
        MedDepartmentFlowEntity entity = BeanUtil.toBean(form, MedDepartmentFlowEntity.class);
        flowRegulationService.save(entity);
        return R.ok("科室调流规则保存成功");
    }

    @GetMapping("/config")
    @Operation(summary = "根据ID查询科室流量配置", description = "根据配置ID查询科室流量调控详细信息")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:SELECT"}, mode = SaMode.OR)
    public R findById(@Valid @ParameterObject GetDeptFlowByIdForm form) {
        MedDepartmentFlowEntity entity = flowRegulationService.getById(form.getId());
        return R.ok().put("result", entity);
    }

    @PutMapping
    @Operation(summary = "更新科室调流规则")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:UPDATE"}, mode = SaMode.OR)
    public R update(@RequestBody @Valid UpdateDeptFlowForm form) {
        Map<String, Object> map = BeanUtil.beanToMap(form);
        flowRegulationService.update(map);
        return R.ok("科室调流规则更新成功");
    }

    @DeleteMapping
    @Operation(summary = "批量删除调流科室配置", description = "根据ID数组批量删除科室流量调控配置")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:DELETE"}, mode = SaMode.OR)
    public R removeByIds(@RequestBody @Valid RemoveDeptFlowByIdsForm form) {
        flowRegulationService.removeByIds(form.getIds());
        return R.ok("删除成功");
    }

    @PutMapping("/mode")
    @Operation(summary = "切换调流模式")
    @SaCheckPermission(value = {"ROOT", "FLOW_REGULATION:UPDATE"}, mode = SaMode.OR)
    public R switchMode(@RequestBody @Valid SwitchFlowRegulationModeForm form) {
        boolean result = sysConfigService.setConfigValue("auto_flow_regulation", String.valueOf(form.getMode()));
        return R.ok().put("result", result);
    }

}
