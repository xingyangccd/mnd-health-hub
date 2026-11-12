package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_permission
 */
@Data
public class SysPermissionEntity {
    private Integer permissionId;

    private String permissionCode;

    private Integer moduleId;

    private Integer actionId;
}