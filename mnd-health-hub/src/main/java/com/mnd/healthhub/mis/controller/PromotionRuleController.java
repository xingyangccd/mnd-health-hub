package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.db.entity.PromotionRuleEntity;
import com.mnd.healthhub.mis.controller.form.*;
import com.mnd.healthhub.mis.service.PromotionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("misRuleController")
@RequestMapping("/mis/rule")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "规则管理")
public class PromotionRuleController {

    private final PromotionRuleService promotionRuleService;

    @GetMapping("/list")
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查看促销规则列表", operationId = "ruleList")
    public R list() {
        List<Map<String, Object>> ruleList = promotionRuleService.listAll();
        return R.ok().put("ruleList", ruleList);
    }

    @GetMapping("/page")
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "分页查询", description = "根据规则名称模糊查询促销规则")
    public R page(@ParameterObject @Valid RulePageQueryForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = promotionRuleService.queryPage(params);
        return R.ok().put("pageResult", pageResult);
    }

    @PostMapping
    @SaCheckPermission(value = {"ROOT", "RULE:INSERT"}, mode = SaMode.OR)
    @Operation(summary = "保存促销规则", description = "新增一条促销规则")
    public R save(@RequestBody @Valid SavePromotionRuleForm form) {
        // form 转换成实体 bean
        PromotionRuleEntity rule = BeanUtil.toBean(form, PromotionRuleEntity.class);
        promotionRuleService.save(rule);
        return R.ok("保存成功");
    }

    @GetMapping
    @SaCheckPermission(value = {"ROOT", "RULE:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "查询规则详情", description = "根据规则id获取规则的详细信息")
    public R getById(@ParameterObject @Valid GetRuleByIdForm form) {
        PromotionRuleEntity rule = promotionRuleService.getById(form.getRuleId());
        return R.ok().put("result", rule);
    }

    @PutMapping
    @SaCheckPermission(value = {"ROOT", "RULE:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "修改规则", description = "修改促销规则信息")
    public R update(@RequestBody @Valid UpdateRuleForm form) {
        PromotionRuleEntity rule = BeanUtil.toBean(form, PromotionRuleEntity.class);
        promotionRuleService.updateById(rule);
        return R.ok("规则更新成功");
    }

    @DeleteMapping
    @Operation(summary = "删除规则", description = "根据规则ID删除促销规则", operationId = "ruleDelete")
    @SaCheckPermission(value = {"ROOT", "RULE:DELETE"}, mode = SaMode.OR)
    public R removeById(@ParameterObject @Valid RemoveRuleByIdForm form) {
        promotionRuleService.removeById(form.getRuleId());
        return R.ok("规则删除成功");
    }

}
