package com.mnd.healthhub.db.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName med_exam_package
 */
@Data
public class MedExamPackageEntity {
    private Integer id;

    private String packageCode;

    private String packageName;

    private String description;

    private String departmentExam;

    private String labExam;

    private String medicalExam;

    private String otherExam;

    private String examItems;

    private String coverImage;

    private BigDecimal originalPrice;

    private BigDecimal currentPrice;

    private Integer salesVolume;

    private String packageType;

    private String tags;

    private Integer categoryId;

    private Integer promotionId;

    private Integer status;

    private String md5Hash;

    private String updateTime;

    private String createTime;
}