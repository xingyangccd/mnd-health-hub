package com.mnd.healthhub.mcp.tools;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.IdcardUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedExamAppointmentEntity;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.AppointmentService;
import com.mnd.healthhub.front.service.TradeOrderService;
import com.mnd.healthhub.mcp.config.McpAuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 体检预约 MCP 工具
 * <p>
 * 所有需要登录的工具通过 McpAuthUtil 从当前对话上下文中解析客户ID。
 */
@Service
@RequiredArgsConstructor
public class AppointmentMcpTools {

    private final AppointmentService appointmentService;
    private final TradeOrderService tradeOrderService;
    private final com.mnd.healthhub.mis.service.AppointmentService misAppointmentService;

    /**
     * 创建体检预约（需要登录）
     */
    @Tool(description = "为已下单的客户创建体检预约。需要登录，客户ID从当前登录状态自动解析。预约日期必须在未来60天内。")
    public String createAppointment(
            @ToolParam(description = "订单ID（支付完成的订单）", required = true)
            Integer orderId,
            @ToolParam(description = "预约日期，格式 yyyy-MM-dd，比如 2026-09-20", required = true)
            String appointmentDate,
            @ToolParam(description = "患者姓名，2-10个汉字", required = true)
            String patientName,
            @ToolParam(description = "患者身份证号，18位", required = true)
            String idCardNo,
            @ToolParam(description = "患者手机号，11位", required = true)
            String phone,
            @ToolParam(description = "详细地址", required = true)
            String address,
            @ToolParam(description = "公司名称，选填", required = false)
            String company) {

        int customerId = McpAuthUtil.requireCustomerId();

        // 校验订单归属
        tradeOrderService.checkOrderOwnership(orderId, customerId);

        // 校验身份证
        if (!IdcardUtil.isValidCard18(idCardNo)) {
            return "身份证号无效";
        }

        // 提取生日和性别
        String birthDate = IdcardUtil.getBirthDate(idCardNo).toDateStr();
        String gender = IdcardUtil.getGenderByIdCard(idCardNo) == 1 ? "男" : "女";

        // 构建预约实体
        MedExamAppointmentEntity appointment = new MedExamAppointmentEntity();
        appointment.setOrderId(orderId);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setPatientName(patientName);
        appointment.setIdCardNo(idCardNo);
        appointment.setPhone(phone);
        appointment.setAddress(address);
        appointment.setCompany(company);
        appointment.setAppointmentNo(IdUtil.simpleUUID().toUpperCase());
        appointment.setBirthDate(birthDate);
        appointment.setGender(gender);

        String result = appointmentService.save(appointment);
        return switch (result) {
            case "预约成功" -> "预约成功！预约编号：" + appointment.getAppointmentNo();
            case "当天预约已满，请选择其它日期" -> "当天预约已满，请选择其它日期";
            default -> "预约失败：" + result;
        };
    }

    /**
     * 分页查询预约列表（需要登录，只查当前用户的）
     */
    @Tool(description = "分页查询当前登录客户的体检预约记录列表，支持按状态筛选。需要登录，客户ID从当前登录状态自动解析。")
    public Map<String, Object> listAppointments(
            @ToolParam(description = "页码，从1开始", required = true)
            Integer pageNo,
            @ToolParam(description = "每页大小", required = true)
            Integer pageSize,
            @ToolParam(description = "预约状态：1-未签到，2-已签到，3-已完成，选填", required = false)
            String status) {

        int customerId = McpAuthUtil.requireCustomerId();

        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customerId);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("startIndex", (pageNo - 1) * pageSize);
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }

        PageResult<Map<String, Object>> pageResult = appointmentService.queryPage(params);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", pageResult.getRecords());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 查询预约详情（需要登录，通过订单归属校验）
     */
    @Tool(description = "根据预约编号查询预约的完整详细信息，包含体检人信息、预约状态等。需要登录，只允许查询当前用户自己的预约。")
    public Map<String, Object> getAppointmentDetail(
            @ToolParam(description = "预约编号，如 F7ADC0D6C1AB4E18A5B7C72005E72AA5", required = true)
            String appointmentNo) {

        int customerId = McpAuthUtil.requireCustomerId();

        // 查询预约详情
        Map<String, Object> detail = misAppointmentService.getByAppointmentNo(appointmentNo);

        // 通过 orderId 校验预约归属
        Object orderIdObj = detail.get("orderId");
        if (orderIdObj == null) {
            throw new HisException("预约数据异常：缺少订单信息", 500);
        }
        Integer orderId = orderIdObj instanceof Integer
                ? (Integer) orderIdObj
                : Integer.parseInt(orderIdObj.toString());
        tradeOrderService.checkOrderOwnership(orderId, customerId);

        return detail;
    }
}