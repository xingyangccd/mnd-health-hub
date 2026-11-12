package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.SysRoleEntity;

import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【sys_role(系统角色表)】的数据库操作Mapper
* @createDate 2026-08-14 16:46:04
* @Entity com.mnd.healthhub.db.entity.SysRoleEntity
*/
public interface SysRoleMapper {
    /**
     * 查询所有的角色
     * @return 角色列表
     */
    List<Map<String, Object>> selectAll();
}




