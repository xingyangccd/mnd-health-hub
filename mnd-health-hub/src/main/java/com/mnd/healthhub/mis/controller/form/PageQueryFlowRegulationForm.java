package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "分页查询流量调控请求参数")
public class PageQueryFlowRegulationForm {

    @Schema(description = "科室名称", example = "内科")
    @Pattern(regexp = "^$|^[0-9a-zA-Z\\u4e00-\\u9fa5\\(\\)]{1,40}$", message = "deptName内容不正确")
    private String deptName;

    @Schema(description = "信标UUID", example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    @Pattern(regexp = "^$|^[0-9a-zA-Z]{32}$", message = "beaconUuid内容不正确")
    private String beaconUuid;

    @Schema(description = "当前页码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "pageNo不能为空")
    @Min(value = 1, message = "pageNo不能小于1")
    private Integer pageNo;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "pageSize不能为空")
    @Range(min = 10, max = 50, message = "pageSize必须为10~50之间")
    private Integer pageSize;
}