package com.mnd.healthhub.front.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "业务端预约分页查询请求参数")
public class PageQueryAppointmentForm {

    @Schema(description = "搜索关键字（套餐名称/体检人姓名/电话）", example = "体检", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\u4e00-\\u9fa5]{1,50}$", message = "keyword内容不正确")
    private String keyword;

    @Schema(description = "预约状态（1=未签到，3=已完成）", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^1$|^3$", message = "status内容不正确")
    private String status;

    @Schema(description = "预约日期", example = "2026-09-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "appointmentDate内容不正确")
    private String appointmentDate;

    @Schema(description = "当前页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @Schema(description = "每页条数（10~50）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}