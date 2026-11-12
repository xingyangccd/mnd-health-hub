package com.mnd.healthhub.db.entity;

import java.util.Date;
import lombok.Data;

/**
 * @TableName sys_user
 */
@Data
public class SysUserEntity {
    private Integer userId;

    private String username;

    private String password;

    private String realName;

    private String gender;

    private String mobile;

    private String email;

    private String avatar;

    private String openId;

    private Integer deptId;

    private String hireDate;

    private String roleIds;

    private Integer isSuperAdmin;

    private Integer userStatus;

    private String createTime;
}