package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName crm_customer_im
 */
@Data
public class CrmCustomerImEntity {
    private Integer id;

    private Integer customerId;

    private String lastLoginTime;
}