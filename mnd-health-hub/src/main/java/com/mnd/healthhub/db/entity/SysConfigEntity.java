package com.mnd.healthhub.db.entity;

import lombok.Data;

/**
 * @TableName sys_config
 */
@Data
public class SysConfigEntity {
    private Integer configId;

    private String configKey;

    private String configValue;

    private String remark;
}