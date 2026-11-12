package com.mnd.healthhub.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.IdcardUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.db.entity.MedExamAppointmentEntity;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.controller.form.PageQueryAppointmentForm;
import com.mnd.healthhub.front.controller.form.SaveAppointmentForm;
import com.mnd.healthhub.front.service.AppointmentService;
import com.mnd.healthhub.front.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("frontAppointmentController")
@RequestMapping("/front/appointment")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "业务端预约管理")
public class AppointmentController {

    private final TradeOrderService tradeOrderService;
    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "创建预约")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R createAppointment(@RequestBody @Valid SaveAppointmentForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        // 校验订单的归属权（没有抛出异常说明拥有该订单）
        tradeOrderService.checkOrderOwnership(form.getOrderId(), customerId);
        // 验证身份证是否有效
        String idCardNo = form.getIdCardNo();
        if(!IdcardUtil.isValidCard18(idCardNo)) {
            throw new HisException("身份证号无效");
        }
        // 从身份证号中提取生日和性别
        String birthDate = IdcardUtil.getBirthDate(idCardNo).toDateStr();
        String gender = IdcardUtil.getGenderByIdCard(idCardNo) == 1 ? "男" : "女";
        // 验证预约日期必须在从次日起的60天之内。
        DateTime appointmentDate = DateUtil.parse(form.getAppointmentDate());
        DateTime startDate = DateUtil.beginOfDay(DateUtil.tomorrow()); // 为什么要把时间重置到这个日期的零点
        DateTime endDate = DateUtil.offsetDay(startDate, 59);
        if(!appointmentDate.isIn(startDate, endDate)){
            throw new HisException("预约日期必须在未来60天以内");
        }
        // 构建预约对象。
        MedExamAppointmentEntity appointment = BeanUtil.toBean(form, MedExamAppointmentEntity.class);
        appointment.setAppointmentNo(IdUtil.simpleUUID().toUpperCase());
        appointment.setBirthDate(birthDate);
        appointment.setGender(gender);

        // 结果是三种情况：SUCCESS FAIL FULL
        String result = appointmentService.save(appointment);
        return R.ok().put("result", result);
    }

    @GetMapping("/list")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "分页查询", operationId = "appointmentPage")
    public R pageQuery(@ParameterObject @Valid PageQueryAppointmentForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        int pageNo = form.getPageNo();
        int pageSize = form.getPageSize();
        int startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> param = BeanUtil.beanToMap(form);
        param.put("startIndex", startIndex);
        param.put("customerId", customerId);
        PageResult<Map<String, Object>> pageResult = appointmentService.queryPage(param);
        return R.ok().put("pageResult", pageResult);
    }
}
















