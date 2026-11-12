package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.CrmCustomerEntity;

import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【crm_customer(客户信息表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.CrmCustomerEntity
 */
public interface CrmCustomerMapper {

    /**
     * 保存客户信息
     *
     * @param customer 客户信息
     * @return 1表示保存成功
     */
    int insert(CrmCustomerEntity customer);

    /**
     * 根据手机号获取用户id
     *
     * @param phone 手机号
     * @return 用户id
     */
    Integer findIdByPhone(String phone);

    /**
     * 根据id获取客户的信息
     *
     * @param id 客户id
     * @return 客户信息
     */
    Map<String, Object> selectById(Integer id);

    /**
     * 更新客户的信息
     * @param param 客户信息
     * @return 1表示成功
     */
    int update(Map<String, Object> param);
}




