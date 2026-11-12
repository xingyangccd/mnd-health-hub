package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "订单分页查询请求参数")
public class OrderPageQueryForm {

    @Schema(description = "手机号", example = "13800138000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^$|^1[1-9]\\d{9}$", message = "phone内容不正确")
    private String phone;

    @Schema(description = "套餐编码", example = "PACK2024001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^$|^[a-zA-Z0-9]{6,20}$", message = "packageCode内容不正确")
    private String packageCode;

    @Schema(description = "关键词（用于模糊搜索）", example = "体检", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\u4e00-\\u9fa5]{1,50}$", message = "keyword内容不正确")
    private String keyword;

    @Schema(description = "订单状态：1-待支付，2-已支付，3-已取消，4-已完成，5-退款中，6-已退款", example = "1", allowableValues = {"1", "2", "3", "4", "5", "6"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Range(min = 1, max = 6, message = "orderStatus内容不正确")
    private Integer orderStatus;

    @Schema(description = "开始日期（格式：YYYY-MM-DD）", example = "2026-08-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "startDate内容不正确")
    private String startDate;

    @Schema(description = "结束日期（格式：YYYY-MM-DD）", example = "2026-08-28", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "endDate内容不正确")
    private String endDate;

    @Min(value = 1, message = "customerId最小值为1")
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1001", minimum = "1")
    private Integer customerId;

    @Schema(description = "当前页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @Schema(description = "每页记录数（5-50）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pageSize不能为空")
    @Range(min = 5, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}