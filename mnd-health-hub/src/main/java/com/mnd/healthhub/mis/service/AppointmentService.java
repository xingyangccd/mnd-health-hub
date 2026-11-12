package com.mnd.healthhub.mis.service;

import com.mnd.healthhub.common.PageResult;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    /**
     * 根据订单id获取该订单下的预约列表
     *
     * @param orderId 订单id
     * @return 预约列表
     */
    List<Map<String, Object>> getByOrderId(Integer orderId);

    /**
     * MIS端分页查询预约记录
     *
     * @param param 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> param);

    /**
     * 批量删除预约记录
     *
     * @param ids 记录id集合
     */
    void removeByIds(Integer[] ids);

    /**
     * 查看当前体检人今天是否有预约。
     *
     * @param params 身份证号、姓名、性别.
     * @return 0表示今日无预约 -1表示已签到  1表示有预约未签到
     */
    int hasAppointmentInToday(Map<String, Object> params);

    /**
     * 体检签到
     *
     * @param params 参数
     * @return true表示签到成功,false失败
     */
    boolean checkin(Map<String, Object> params);

    /**
     * 根据预约ID加载体检人体检导引单
     *
     * @param id 预约ID（主键值）
     * @return 导引单信息
     */
    Map<String, Object> loadGuidanceInfo(Integer id);

    /**
     * 更新预约状态
     *
     * @param params 参数
     * @return true表示操作成功，false表示操作失败
     */
    boolean updateAppointmentStatus(Map<String, Object> params);

    /**
     * 根据预约编号/体检编号获取体检人的信息
     *
     * @param appointmentNo 体检编号
     * @return 体检人的信息
     */
    Map<String, Object> getByAppointmentNo(String appointmentNo);

}
