package com.mnd.healthhub.mis.service;

import java.util.List;
import java.util.Map;

public interface SysRoleService {
    /**
     * 获取所有的角色列表
     * @return 角色列表
     */
    List<Map<String, Object>> listAll();
}
