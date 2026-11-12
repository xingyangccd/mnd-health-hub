package com.mnd.healthhub.front.service;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.MedExamAppointmentEntity;

import java.util.Map;

public interface AppointmentService {

    /**
     * 保存体检预约（实现业务端客户预约体检的核心业务方法）
     *
     * @param appointment 预约实体对象
     * @return 预约结果：SUCCESS / FULL / FAIL
     */
    String save(MedExamAppointmentEntity appointment);

    /**
     * 业务端分页查询预约记录
     *
     * @param param 查询条件
     * @return 预约的分页数据
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> param);

}
