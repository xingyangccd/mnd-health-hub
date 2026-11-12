package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.SysConfigEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【sys_config(系统配置表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.SysConfigEntity
 */
public interface SysConfigMapper {

    /**
     * 查询系统所有的默认配置
     *
     * @return 默认配置列表
     */
    List<SysConfigEntity> selectAll();

    /**
     * 根据配置键更新配置值
     *
     * @param params 配置键
     * @return 1表示更新成功
     */
    int updateByConfigKey(Map<String, Object> params);
}




