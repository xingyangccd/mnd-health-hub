package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName crm_customer_location
 */
@Data
public class CrmCustomerLocationEntity {
    private Integer id;

    private Integer customerId;

    private String beaconUuid;

    private Integer departmentId;

    private String createTime;
}