package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "分页查询体检报告请求参数")
public class PageQueryReportForm {

    @Schema(description = "患者姓名", example = "张三", pattern = "^[\\u4e00-\\u9fa5]{1,10}$")
    @Pattern(regexp = "^$|^[\\u4e00-\\u9fa5]{1,10}$", message = "patientName内容不正确")
    private String patientName;

    @Schema(description = "手机号码", example = "13800138000", pattern = "^1[1-9]\\d{9}$")
    @Pattern(regexp = "^$|^1[1-9]\\d{9}$", message = "phone内容不正确")
    private String phone;

    @Schema(description = "快递单号", example = "SF1234567890", pattern = "^[0-9A-Za-z]{10,24}$")
    @Pattern(regexp = "^$|^[0-9A-Za-z]{10,24}$", message = "expressNo内容不正确")
    private String expressNo;

    @Schema(description = "报告状态：1-待处理，2-处理中，3-已完成", example = "1", allowableValues = {"1", "2", "3"})
    @Range(min = 1, max = 3, message = "status内容不正确")
    private Integer status;

    @Schema(description = "当前页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @Schema(description = "每页记录数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "10", maximum = "50")
    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}