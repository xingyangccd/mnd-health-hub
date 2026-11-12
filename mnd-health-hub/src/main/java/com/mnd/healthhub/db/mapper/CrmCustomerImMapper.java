package com.mnd.healthhub.db.mapper;

/**
 * @author Administrator
 * @description 针对表【crm_customer_im(客户IM账号表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.CrmCustomerImEntity
 */
public interface CrmCustomerImMapper {

    /**
     * 保存或更新客户在IM上最后一次登录时间
     *
     * @param customerId 客户id
     * @return 1表示保存成功，2表示更新成功，其他值表示失败。
     */
    int insertOrUpdate(Integer customerId);

}




