package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_role
 */
@Data
public class SysRoleEntity {
    private Integer roleId;

    private String roleName;

    private String roleDesc;

    private String permissionIds;

    private String defaultPermissions;

    private Integer isSystem;
}