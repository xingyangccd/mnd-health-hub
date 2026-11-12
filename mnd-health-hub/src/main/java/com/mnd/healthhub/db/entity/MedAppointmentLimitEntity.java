package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName med_appointment_limit
 */
@Data
public class MedAppointmentLimitEntity {
    private Integer id;

    private String appointmentDate;

    private Integer actualLimit;

    private Integer maxLimit;

    private Integer actualCount;

    private String remark;

    private String createTime;
}