package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName crm_customer
 */
@Data
public class CrmCustomerEntity {
    private Integer id;

    private String customerName;

    private String gender;

    private String phone;

    private String photoUrl;

    private String registerTime;
}