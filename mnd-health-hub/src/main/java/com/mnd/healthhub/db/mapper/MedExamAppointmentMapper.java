package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.MedExamAppointmentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【med_exam_appointment(体检预约记录表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.MedExamAppointmentEntity
 */
public interface MedExamAppointmentMapper {

    /**
     * 根据订单id获取该订单下的预约列表
     *
     * @param orderId 订单id
     * @return 预约列表
     */
    List<Map<String, Object>> selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 保存预约记录
     *
     * @param appointment 预约记录
     * @return 1表示保存成功
     */
    int insert(MedExamAppointmentEntity appointment);

    /**
     * 业务端查询符合条件的预约记录。
     *
     * @param params 查询条件
     * @return 预约记录列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> params);

    /**
     * 业务端查询符合条件的预约记录的总数
     *
     * @param params 查询条件
     * @return 预约总数
     */
    long selectPageCount(Map<String, Object> params);

    /**
     * 业务端查询符合条件的预约记录。
     *
     * @param params 查询条件
     * @return 预约记录列表
     */
    List<Map<String, Object>> selectPageListForMis(Map<String, Object> params);

    /**
     * 业务端查询符合条件的预约记录的总数
     *
     * @param params 查询条件
     * @return 预约总数
     */
    long selectPageCountForMis(Map<String, Object> params);

    /**
     * 批量删除预约记录
     *
     * @param ids 记录id
     * @return 删除的个数
     */
    int deleteByIds(@Param("ids") Integer[] ids);

    /**
     * 查询是否当天有预约
     *
     * @param params 身份证号、体检人姓名、体检人性别
     * @return 预约记录的状态(status)
     */
    Map<String, Object> selectAppointInToday(Map<String, Object> params);

    /**
     * 更新预约状态
     *
     * @param params 参数
     * @return 1表示更新成功
     */
    int updateForCheckin(Map<String, Object> params);

    /**
     * 查询预约编号以及商品快照id
     *
     * @param params 参数
     * @return 商品快照id和预约编号
     */
    Map<String, Object> selectAppointNoAndSnapshotId(Map<String, Object> params);

    /**
     * 根据预约ID查询体检人的摘要信息
     *
     * @param id 预约ID（主键值）
     * @return 体检人的摘要信息
     */
    Map<String, Object> selectSummaryById(Integer id);

    /**
     * 根据预约编号，更新预约记录的状态
     *
     * @param params 预约编号
     * @return 1表示更新成功
     */
    int updateStatusByAppointNo(Map<String, Object> params);

    /**
     * 根据预约编号查询预约ID。
     *
     * @param appointmentNo 预约编号
     * @return 预约ID
     */
    int findIdByAppointNo(String appointmentNo);

    /**
     * 根据体检编号查询体检人的信息
     *
     * @param appointmentNo 体检编号
     * @return 体检人的信息
     */
    Map<String, Object> selectByAppointmentNo(String appointmentNo);

    /**
     * 根据ID查询体检人的信息、套餐名、预约编号/体检编号
     *
     * @param id 主键ID
     * @return 体检人信息、套餐名、体检编号
     */
    Map<String, Object> selectDataForReport(Integer id);
}




