package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.PasswordEncryptor;
import com.mnd.healthhub.db.entity.SysUserEntity;
import com.mnd.healthhub.db.mapper.SysUserMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    private final SysUserMapper sysUserMapper;

    @Override
    public Integer login(String username, String password) {
        // 加密密码
        String encryptedPassword = PasswordEncryptor.encryptWithUsernameSalt(username, password);
        // 验证用户名和密码
        Integer userId = sysUserMapper.findUserIdByCredentials(username, encryptedPassword);
        if (userId == null) {
            // 401一般是项目中的认证失败/登录失败的错误编码。
            // 在这里不需要记录错误日志/警告日志，因为全局异常处理中记录了。
            throw new HisException("用户名或密码错误", 401);
        }
        // 登录记录日志
        log.info("登录成功：userId={}", userId);
        return userId;
    }

    @Override
    @Transactional
    public void updatePassword(int userId, String oldPassword, String newPassword) {
        String username = sysUserMapper.findUsernameByUserId(userId);
        if (username == null) {
            // 这里不需要记录日志，因为在全局异常处理中已经记录了。
            throw new HisException("用户名不存在", 404);
        }
        // 用户名是存在的
        String encryptOldPassword = PasswordEncryptor.encryptWithUsernameSalt(username, oldPassword);
        String encryptNewPassword = PasswordEncryptor.encryptWithUsernameSalt(username, newPassword);
        int rows = sysUserMapper.updatePassword(encryptOldPassword, encryptNewPassword, userId);
        if (rows != 1) {
            throw new HisException("密码修改失败", 400);
        }
        // 修改密码成功
        log.info("密码修改成功：userId={}", userId);
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> params) {
        // 查询总记录条数
        Long total = sysUserMapper.selectPageCount(params);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            // 总记录条数是大于0的，表示这样的查询结果是存在的，因此再查询数据
            records = sysUserMapper.selectPageList(params);
        }
        // 返回分页对象
        return PageResult.of(records, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysUserEntity user) {
        Integer userId = sysUserMapper.findUserIdByUsername(user.getUsername());
        if (userId != null) {
            throw new HisException("用户名已存在", 400);
        }
        // 对用户密码进行加密
        String password = PasswordEncryptor.encryptWithUsernameSalt(user.getUsername(), user.getPassword());
        user.setPassword(password);
        int rows = sysUserMapper.insert(user);
        if (rows != 1) {
            throw new HisException("用户信息保存失败", 500);
        }
        log.info("用户信息保存成功：username={}", user.getUsername());
    }

    @Override
    public Map<String, Object> getById(Integer userId) {
        Map<String, Object> user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new HisException("用户不存在", 404);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Map<String, Object> user) {
        int rows = sysUserMapper.update(user);
        if (rows != 1) {
            throw new HisException("用户信息修改失败", 400);
        }
        log.info("用户信息修改成功：userId={}", user.get("id"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeByIds(Integer[] ids) {
        Integer adminId = sysUserMapper.findSuperAdminId();
        if (ArrayUtil.contains(ids, adminId)) {
            throw new HisException("超级管理员不能删除", 400);
        }
        // 被删除用户中没有超管，可以正常删除
        int rows = sysUserMapper.deleteByIds(ids);
        log.info("删除了{}条数据", rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dismiss(Integer userId) {
        int rows = sysUserMapper.updateStatus(userId);
        if (rows != 1) {
            throw new HisException("员工办理离职失败", 400);
        }
        log.info("员工已经成功办理离职：userId={}", userId);
    }

    @Override
    public Map<String, Object> getDoctorById(int id) {
        Map<String, Object> map = sysUserMapper.selectDoctorById(id);
        if(map == null) {
            throw new HisException("医生信息不存在", 400);
        }
        return map;
    }
}
