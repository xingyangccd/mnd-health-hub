package com.mnd.healthhub.mis.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "修改流量调控配置请求参数")
public class UpdateDeptFlowForm {

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "id不能为空")
    @Min(value = 1, message = "id不能小于1")
    private Integer id;

    @Schema(description = "科室名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "内科")
    @NotBlank(message = "departmentName不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\(\\)]{2,40}$", message = "departmentName内容不正确")
    private String departmentName;

    @Schema(description = "信标UUID", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6")
    @NotBlank(message = "beaconUuid不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{32}$", message = "beaconUuid内容不正确")
    private String beaconUuid;

    @Schema(description = "最大容量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "maxCapacity不能为空")
    @Range(min = 1, max = 1000, message = "maxCapacity内容不正确")
    private Integer maxCapacity;

    @Schema(description = "权重", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "weight不能为空")
    @Range(min = 1, max = 10, message = "weight内容不正确")
    private Integer weight;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "priority不能为空")
    @Range(min = 1, max = 10, message = "priority内容不正确")
    private Integer priority;
}