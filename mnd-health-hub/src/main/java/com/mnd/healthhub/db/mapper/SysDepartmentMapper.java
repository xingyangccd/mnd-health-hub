package com.mnd.healthhub.db.mapper;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【sys_department(部门信息表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.SysDepartmentEntity
 */
public interface SysDepartmentMapper {
    /**
     * 查询所有部门信息
     *
     * @return 部门列表
     */
    List<Map<String, Object>> selectAll();
}




