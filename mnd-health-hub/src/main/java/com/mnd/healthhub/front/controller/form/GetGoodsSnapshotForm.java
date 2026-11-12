package com.mnd.healthhub.front.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "查询商品快照请求参数")
public class GetGoodsSnapshotForm {

    @Schema(description = "商品快照ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "507f1f77bcf86cd799439011")
    @NotBlank(message = "snapshotId不能为空")
    @Pattern(regexp = "^[0-9a-z]{24}$", message = "snapshotId内容不正确")
    private String snapshotId;
}