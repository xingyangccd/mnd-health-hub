package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.SysUserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface SysUserService {
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功之后返回userId，登录失败抛异常
     */
    Integer login(String username, String password);

    /**
     * 修改密码
     *
     * @param userId      用户id
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void updatePassword(int userId, String oldPassword, String newPassword);

    /**
     * 分页查询用户信息
     *
     * @param params 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> params);

    /**
     * 保存用户信息
     *
     * @param user 用户信息
     */
    void save(SysUserEntity user);

    /**
     * 根据用户id查询用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    Map<String, Object> getById(Integer userId);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     */
    void update(Map<String, Object> user);

    /**
     * 批量删除用户信息
     *
     * @param ids 用户id集合
     * @return 返回删除的记录条数
     */
    int removeByIds(Integer[] ids);

    /**
     * 为用户办理离职
     *
     * @param userId 用户id
     */
    void dismiss(Integer userId);

    /**
     * 根据ID获取医生的信息
     *
     * @param id 主键ID
     * @return 医生的信息
     */
    Map<String, Object> getDoctorById(int id);
}
