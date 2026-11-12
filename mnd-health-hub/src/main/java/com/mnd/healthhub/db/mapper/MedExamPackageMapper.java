package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.MedExamPackageEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【med_exam_package(体检套餐表)】的数据库操作Mapper
 * @createDate 2026-08-14 16:46:04
 * @Entity com.mnd.healthhub.db.entity.MedExamPackageEntity
 */
public interface MedExamPackageMapper {

    /**
     * 查询符合条件的总记录条数
     *
     * @param param 查询条件
     * @return 总记录条数
     */
    long selectPageCount(Map<String, Object> param);

    /**
     * 查询符合条件的记录
     *
     * @param param 查询条件
     * @return 记录列表
     */
    List<Map<String, Object>> selectPageList(Map<String, Object> param);

    /**
     * 根据商品的哈希值查询商品信息
     *
     * @param md5Hash 商品哈希值
     * @return 商品实体对象
     */
    MedExamPackageEntity selectByMd5Hash(@Param("md5Hash") String md5Hash);

    /**
     * 保存套餐的信息
     *
     * @param goods 套餐信息
     * @return 1表示保存成功
     */
    int insert(MedExamPackageEntity goods);

    /**
     * 根据id、status查询体检套餐信息
     *
     * @param params 查询条件
     * @return 套餐信息
     */
    Map<String, Object> selectById(Map<String, Object> params);

    /**
     * 更新套餐信息
     *
     * @param goods 套餐信息
     * @return 1表示更新成功
     */
    int update(MedExamPackageEntity goods);

    /**
     * 根据id查询套餐信息（适用于excel导入相关功能）
     *
     * @param id 套餐id
     * @return 套餐实体
     */
    MedExamPackageEntity selectEntityById(Integer id);

    /**
     * 更新套餐的exam_items字段
     *
     * @param params 包含md5Hash、examItems、id
     * @return
     */
    int updateExamItems(Map<String, Object> params);

    /**
     * 更新套餐的状态（上架、下架）
     *
     * @param params 参数包含id和status
     * @return 1表示更新成功
     */
    int updateStatus(Map<String, Object> params);

    /**
     * 根据id查询对应的套餐封面图片的路径
     *
     * @param ids 套餐id
     * @return 封面图片路径集合
     */
    List<String> selectImagesByIds(@Param("ids") Integer[] ids);

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id
     * @return 删除的记录条数
     */
    int deleteByIds(@Param("ids") Integer[] ids);

    /**
     * 查询指定分类下销量排名前4的套餐数据
     *
     * @param categoryId1 活动专区1
     * @param categoryId2 热卖套餐2
     * @param categoryId3 新品推荐3
     * @return 12条商品数据
     */
    List<Map<String, Object>> selectTop4ByCategoryId(@Param("categoryId1") Integer categoryId1,
                                                     @Param("categoryId2") Integer categoryId2,
                                                     @Param("categoryId3") Integer categoryId3);

    /**
     * 业务端：查询符合条件的商品信息
     *
     * @param params 查询条件
     * @return 符合条件的商品列表
     */
    List<Map<String, Object>> selectPageListForFront(Map<String, Object> params);

    /**
     * 业务端：查询符合条件的商品总数
     *
     * @param params 查询条件
     * @return 总数
     */
    long selectPageCountForFront(Map<String, Object> params);

    /**
     * 查询商品信息及促销规则（用于创建订单）
     *
     * @param id 商品id
     * @return 商品信息及促销规则
     */
    Map<String, Object> selectPackageWithPromotionForOrder(Integer id);

    /**
     * 更新商品的销量
     *
     * @param goodsId  商品id
     * @param quantity 销量
     * @return 1表示更新成功
     */
    int updateSalesVolume(@Param("goodsId") Integer goodsId, @Param("quantity") Integer quantity);

    /**
     * 根据促销规则查询该促销规则下的套餐数量。
     * @param ruleId 促销规则的id
     * @return 套餐的数量
     */
    Integer selectCountByPromotionId(Integer ruleId);
}




