package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_resource_module
 */
@Data
public class SysResourceModuleEntity {
    private Integer moduleId;

    private String moduleCode;

    private String moduleName;
}