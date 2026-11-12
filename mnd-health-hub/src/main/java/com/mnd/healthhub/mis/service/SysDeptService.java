package com.mnd.healthhub.mis.service;

import java.util.List;
import java.util.Map;

public interface SysDeptService {

    /**
     * 查找所有的部门信息
     * @return 部门列表
     */
    List<Map<String,Object>> listAll();
}
