package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.map.MapUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedDepartmentFlowEntity;
import com.mnd.healthhub.db.mapper.MedDepartmentFlowMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mis.service.FlowRegulationService;
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
public class FlowRegulationServiceImpl implements FlowRegulationService {

    private final MedDepartmentFlowMapper medDepartmentFlowMapper;

    @Override
    public List<String> getDeptNames() {
        return medDepartmentFlowMapper.selectDeptNames();
    }

    @Override
    public PageResult<MedDepartmentFlowEntity> queryPage(Map<String, Object> params) {
        long total = medDepartmentFlowMapper.selectPageCount(params);
        List<MedDepartmentFlowEntity> records = new ArrayList<>();
        if (total > 0) {
            records = medDepartmentFlowMapper.selectPageList(params);
        }
        return PageResult.of(records, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(MedDepartmentFlowEntity flow) {
        int rows = medDepartmentFlowMapper.insert(flow);
        if (rows != 1) {
            throw new HisException("科室调流规则保存失败", 500);
        }
        log.info("成功保存科室[{}]调流规则", flow.getDepartmentName());
    }

    @Override
    public MedDepartmentFlowEntity getById(Integer id) {
        MedDepartmentFlowEntity entity = medDepartmentFlowMapper.selectById(id);
        if (entity == null) {
            throw new HisException("科室调流规则不存在", 400);
        }
        return entity;
    }

    @Override
    public void update(Map<String, Object> params) {
        int rows = medDepartmentFlowMapper.update(params);
        if (rows != 1) {
            throw new HisException("科室调流规则更新失败", 500);
        }
        log.info("科室调流规则更新成功，科室：{}", MapUtil.getStr(params, "departmentName"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Integer[] ids) {
        int rows = medDepartmentFlowMapper.deleteByIds(ids);
        if (rows != ids.length) {
            throw new HisException("科室调流规则删除失败", 500);
        }
        log.info("科室调流规则删除成功，删除{}条", rows);
    }
}
