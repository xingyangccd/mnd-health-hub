package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_operation_type
 */
@Data
public class SysOperationTypeEntity {
    private Integer id;

    private String operationCode;

    private String operationName;
}