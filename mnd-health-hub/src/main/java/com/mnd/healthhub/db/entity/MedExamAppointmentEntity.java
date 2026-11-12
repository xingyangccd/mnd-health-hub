package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName med_exam_appointment
 */
@Data
public class MedExamAppointmentEntity {
    private Integer id;

    private String appointmentNo;

    private Integer orderId;

    private String appointmentDate;

    private String patientName;

    private String gender;

    private String idCardNo;

    private String birthDate;

    private String phone;

    private String address;

    private String company;

    private Integer status;

    private String checkinTime;

    private String createTime;
}