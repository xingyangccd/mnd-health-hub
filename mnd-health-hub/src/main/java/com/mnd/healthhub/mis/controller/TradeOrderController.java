package com.mnd.healthhub.mis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.controller.form.OrderPageQueryForm;
import com.mnd.healthhub.mis.controller.form.ProcessOfflineRefundForm;
import com.mnd.healthhub.mis.controller.form.SyncPaymentResultForm;
import com.mnd.healthhub.mis.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("misOrderController")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/mis/order")
@Tag(name = "MIS端订单管理")
public class TradeOrderController {
    private final TradeOrderService tradeOrderService;

    @GetMapping("/list")
    @SaCheckPermission(value = {"ROOT", "ORDER:SELECT"}, mode = SaMode.OR)
    @Operation(summary = "分页查询订单列表", description = "支持手机号、套餐编号、套餐名称、订单状态、订单创建起止日期进行查询", operationId = "misOrderPage")
    public R list(@ParameterObject @Valid OrderPageQueryForm form) {
        // 起止日期的校验：要么都是空、要么都不是空、开始日期 < 结束日期
        String startDate = form.getStartDate();
        String endDate = form.getEndDate();
        if ((StrUtil.isNotBlank(startDate) && StrUtil.isBlank(endDate)) || (StrUtil.isBlank(startDate) && StrUtil.isNotBlank(endDate))) {
            throw new HisException("开始日期和结束日期必须同时为空或同时不为空");
        }
        // 程序走到这里说明，要么都是空，要么都不是空。
        if (StrUtil.isNotBlank(startDate)) {
            // 开始日期 < 结束日期
            DateTime start = DateUtil.parseDate(startDate);
            DateTime end = DateUtil.parseDate(endDate);
            if (start.isAfter(end)) {
                throw new HisException("开始日期不能晚于结束日期");
            }
        }

        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> params = BeanUtil.beanToMap(form);
        params.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = tradeOrderService.queryPage(params);
        return R.ok().put("pageResult", pageResult);

    }

    @PostMapping("/sync-results")
    @Operation(summary = "同步支付结果", description = "根据商户的订单流水号批量同步支付结果，返回成功更新的订单数量")
    @SaCheckPermission(value = {"ROOT", "ORDER:UPDATE"}, mode = SaMode.OR)
    public R syncPaymentResults(@RequestBody @Valid SyncPaymentResultForm form) {
        int rows = tradeOrderService.syncPaymentResult(form.getOutTradeNos());
        return R.ok().put("rows", rows);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "根据订单ID删除订单")
    @SaCheckPermission(value = {"ROOT"})
    public R removeById(
            @PathVariable
            @Parameter(description = "订单ID", required = true, example = "1")
            @Min(value = 1, message = "ID不能小于1")
            Integer id) {
        tradeOrderService.removeById(id);
        return R.ok("订单删除成功");
    }

    @PostMapping("/refund/offline")
    @Operation(summary = "线下退款", description = "根据订单流水号在线下完成退款操作")
    @SaCheckPermission(value = {"ROOT", "ORDER:UPDATE"}, mode = SaMode.OR)
    public R refundOffline(@Valid @RequestBody ProcessOfflineRefundForm form) {
        String outTradeNo = form.getOutTradeNo();
        tradeOrderService.processOfflineRefund(outTradeNo);
        return R.ok("退款成功");
    }

}
