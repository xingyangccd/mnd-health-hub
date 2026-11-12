package com.mnd.healthhub.front.service;

import com.mnd.healthhub.common.PageResult;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    /**
     * 业务端的：根据id获取商品详细信息
     *
     * @param id 商品id
     * @return 商品详情
     */
    Map<String, Object> getById(Integer id);

    /**
     * 获取每个分类销量排名前4的套餐数据
     *
     * @param categoryIds 多个分类id
     * @return 套餐列表
     */
    Map<Integer, List<Map<String, Object>>> getTop4CategoryIds(Integer[] categoryIds);

    /**
     * 分页查询商品信息
     *
     * @param params 查询条件
     * @return 分页对象
     */
    PageResult<Map<String, Object>> queryPage(Map<String, Object> params);

    /**
     * 根据快照id获取商品快照信息
     *
     * @param snapshotId 快照id
     * @param customerId 客户id：如果该方法是业务端使用，需要提供该字段。如果是mis端使用，该字段提供null
     * @return 商品快照
     */
    Map<String, Object> getBySnapshotId(String snapshotId, Integer customerId);
}
