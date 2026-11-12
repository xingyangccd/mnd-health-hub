package com.mnd.healthhub.mcp.tools;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.TradeOrderService;
import com.mnd.healthhub.mcp.config.McpAuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单与支付 MCP 工具
 * <p>
 * 所有需要登录的工具通过 McpAuthUtil 从当前对话上下文中解析客户ID。
 */
@Service
@RequiredArgsConstructor
public class OrderMcpTools {

    private final TradeOrderService tradeOrderService;
    private final TradeOrderMapper tradeOrderMapper;

    /**
     * 创建支付订单（需要登录）
     */
    @Tool(description = "为当前登录客户创建支付订单，生成微信支付二维码。下单成功后会自动检查风控规则。需要登录，客户ID从当前登录状态自动解析。")
    public Map<String, Object> createOrder(
            @ToolParam(description = "体检套餐商品ID", required = true)
            Integer goodsId,
            @ToolParam(description = "购买数量，至少1份", required = true)
            Integer quantity) {

        int customerId = McpAuthUtil.requireCustomerId();

        Map<String, Object> result = tradeOrderService.createPayment(customerId, goodsId, quantity);
        if (result == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "订单创建失败：触发风控限制，该客户今日下单已达上限");
            return error;
        }
        result.put("success", true);
        result.put("message", "订单创建成功，请扫码支付");
        return result;
    }

    /**
     * 分页查询订单列表（需要登录，只查当前用户的）
     */
    @Tool(description = "分页查询当前登录客户的订单列表，支持按订单状态和关键词筛选。需要登录，客户ID从当前登录状态自动解析。")
    public Map<String, Object> listOrders(
            @ToolParam(description = "页码，从1开始", required = true)
            Integer pageNo,
            @ToolParam(description = "每页大小", required = true)
            Integer pageSize,
            @ToolParam(description = "订单状态：1-待支付，3-已完成，5-已预约，6-已完成体检，选填", required = false)
            String orderStatus) {

        int customerId = McpAuthUtil.requireCustomerId();

        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customerId);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("startIndex", (pageNo - 1) * pageSize);
        if (orderStatus != null && !orderStatus.isEmpty()) {
            params.put("orderStatus", orderStatus);
        }

        PageResult<Map<String, Object>> pageResult = tradeOrderService.queryPage(params);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", pageResult.getRecords());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 查询订单支付状态（需要登录，严格校验订单归属）
     */
    @Tool(description = "根据订单流水号查询支付结果，返回是否已支付。需要登录，且只允许查询当前用户自己的订单。")
    public Map<String, Object> getPaymentStatus(
            @ToolParam(description = "订单流水号，如 5F8E9A2B3C4D5E6F7G8H9I0J1K2L3M4N", required = true)
            String outTradeNo) {

        int customerId = McpAuthUtil.requireCustomerId();

        // 严格校验订单归属
        Integer ownerId = tradeOrderMapper.findCustomerIdByOutTradeNo(outTradeNo);
        if (ownerId == null) {
            throw new HisException("订单不存在", 404);
        }
        if (!ownerId.equals(customerId)) {
            throw new HisException("无权查看该订单", 403);
        }

        boolean paid = tradeOrderService.getPaymentResult(outTradeNo);

        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", outTradeNo);
        result.put("paid", paid);
        result.put("status", paid ? "已支付" : "未支付");
        return result;
    }

    /**
     * 申请退款（需要登录，校验订单归属）
     */
    @Tool(description = "根据订单ID为当前登录客户申请退款，退款金额将原路返回。需要登录，且只允许退当前用户自己的订单。")
    public String refundOrder(
            @ToolParam(description = "订单ID", required = true)
            Integer orderId) {

        int customerId = McpAuthUtil.requireCustomerId();

        try {
            tradeOrderService.refund(customerId, orderId);
            return "退款申请已提交，请等待微信处理";
        } catch (Exception e) {
            return "退款申请失败：" + e.getMessage();
        }
    }
}