package com.mnd.healthhub.mis.service;

import java.util.List;
import java.util.Map;

public interface CheckupService {
    /**
     * 获取该体检人在当前科室中的体检项目
     *
     * @param uuid  体检编号
     * @param place 科室
     * @return 体检项目列表
     */
    Map<String, Object> getCheckupByPlace(String uuid, String place);

    /**
     * 保存体检结果
     *
     * @param doctorId   医生ID
     * @param doctorName 医生名字
     * @param uuid       体检流水号
     * @param place      科室
     * @param template   模板值
     * @param items       体检结果列表
     */
    void addResult(int doctorId, String doctorName, String uuid, String place, String template, List items);
}
