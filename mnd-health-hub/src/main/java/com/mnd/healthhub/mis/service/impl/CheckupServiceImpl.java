package com.mnd.healthhub.mis.service.impl;

import cn.hutool.core.date.DateUtil;
import com.mnd.healthhub.db.mapper.CheckupResultDao;
import com.mnd.healthhub.mis.service.CheckupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckupServiceImpl implements CheckupService {

    private final CheckupResultDao checkupResultDao;

    @Override
    public Map<String, Object> getCheckupByPlace(String uuid, String place) {
        List<Map> checkupList = checkupResultDao.searchCheckupByPlace(uuid, place);
        boolean hasCheckupResults = checkupResultDao.hasCheckupResults(uuid, place);
        Map<String, Object> map = new HashMap<>();
        map.put("hasCheckupResults", hasCheckupResults);
        map.put("checkupList", checkupList);
        return map;
    }

    @Override
    public void addResult(int doctorId, String doctorName, String uuid, String place, String template, List items) {
        Map map = new HashMap();
        map.put("doctorId", doctorId);
        map.put("doctorName", doctorName);
        map.put("place", place);
        map.put("date", DateUtil.today());
        map.put("template", template);
        map.put("item", items);
        checkupResultDao.addResult(uuid, place, map);
    }
}
