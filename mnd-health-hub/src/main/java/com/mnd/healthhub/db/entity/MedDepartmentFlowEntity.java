package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName med_department_flow
 */
@Data
public class MedDepartmentFlowEntity {
    private Integer id;

    private String departmentName;

    private Integer currentCount;

    private Integer maxCapacity;

    private Integer weight;

    private Integer priority;

    private String beaconUuid;
}