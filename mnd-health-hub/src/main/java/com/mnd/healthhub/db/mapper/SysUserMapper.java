package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.SysUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 * @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.SysUserEntity
 */
public interface SysUserMapper {
    /**
     * 根据用户id查询用户对应的权限列表。
     *
     * @param userId
     * @return
     */
    Set<String> selectPermissionsByUserId(int userId);

    /**
     * 根据认证信息（用户名和密码）查询userId
     * 团队有约定：方法的参数个数如果 &lt= 3个，直接写参数。如果方法的参数个数 &gt; 3 个，封装对象/Map
     * 团队约定：Mapper中方法名的定义，当查询的是实体/列表的时候，方法名 selectXxxx()，当查询结果不是实体/列表，方法名 findXxxx()
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户的id
     */
    Integer findUserIdByCredentials(@Param("username") String username, @Param("password") String password);

    /**
     * 根据userId查询用户名
     *
     * @param userId 用户id
     * @return username
     */
    String findUsernameByUserId(@Param("userId") int userId);

    /**
     * 更新用户的密码
     *
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param userId      用户id
     * @return 1表示更新成功，其他值失败。
     */
    int updatePassword(String oldPassword, String newPassword, int userId);

    /**
     * 查询符合条件的总记录条数。
     *
     * @param params 查询条件集合
     * @return 总记录条数
     */
    Long selectPageCount(Map<String, Object> params);

    /**
     * 查询符合条件的记录
     *
     * @param params 查询条件集合
     * @return 查询结果列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> params);

    /**
     * 根据用户名查询用户id
     *
     * @param username 用户名
     * @return 用户id
     */
    Integer findUserIdByUsername(@Param("username") String username);

    /**
     * 保存用户信息
     *
     * @param user 用户信息
     * @return 1表示保存成功
     */
    int insert(SysUserEntity user);

    /**
     * 根据ID查询用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    Map<String, Object> selectById(@Param("userId") Integer userId);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 1表示更新成功
     */
    int update(Map<String, Object> user);

    /**
     * 批量删除用户信息
     *
     * @param ids 用户的id集合
     * @return 返回删除的记录条数
     */
    int deleteByIds(@Param("ids") Integer[] ids);

    /**
     * 查询超级管理员的id
     *
     * @return 超管的id
     */
    Integer findSuperAdminId();

    /**
     * 更新用户的状态
     *
     * @param userId
     * @return
     */
    int updateStatus(Integer userId);

    /**
     * 根据ID查询医生的信息
     *
     * @param id 主键值
     * @return 医生的信息
     */
    Map<String, Object> selectDoctorById(@Param("id") int id);
}




