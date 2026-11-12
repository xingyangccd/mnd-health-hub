package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.MedAppointmentLimitEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【med_appointment_limit(体检预约限流配置表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.MedAppointmentLimitEntity
 */
public interface MedAppointmentLimitMapper {

    /**
     * 查询指定日期区间所有的限流规则
     *
     * @param startDate 开始日期（包含）
     * @param endDate   结束日期（包含）
     * @return 限流规则列表
     */
    List<Map<String, Object>> selectLimitRulesByDateRange(@Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);

    /**
     * 根据日期查询限流规则
     * @param date 日期
     * @return 限流规则
     */
    Map<String, Object> selectByAppointmentDate(String date);

    /**
     * 保存/修改预约限流记录。
     * @param params 预约限流记录对象
     * @return 1表示保存成功，2表示更新成功，其他值表示失败。
     */
    int insertOrUpdateAppointmentLimit(Map<String, Object> params);
}




