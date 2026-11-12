package com.mnd.healthhub.mis.service.impl;

import com.mnd.healthhub.db.mapper.SysRoleMapper;
import com.mnd.healthhub.mis.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<Map<String, Object>> listAll() {
        return sysRoleMapper.selectAll();
    }
}
