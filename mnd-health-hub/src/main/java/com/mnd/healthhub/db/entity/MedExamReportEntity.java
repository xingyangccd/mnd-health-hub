package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName med_exam_report
 */
@Data
public class MedExamReportEntity {
    private Integer id;

    private Integer appointmentId;

    private String reportId;

    private Integer status;

    private String fileUrl;

    private String expressNo;

    private String expressDate;

    private String examDate;

    private String createTime;
}