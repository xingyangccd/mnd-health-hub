package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_department
 */
@Data
public class SysDepartmentEntity {
    private Integer id;

    private String deptName;

    private String telephone;

    private String email;

    private String remark;
}