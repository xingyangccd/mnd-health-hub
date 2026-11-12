package com.mnd.healthhub.mis.service.impl;

import com.mnd.healthhub.db.mapper.SysDepartmentMapper;
import com.mnd.healthhub.mis.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public List<Map<String, Object>> listAll() {
        return sysDepartmentMapper.selectAll();
    }
}
