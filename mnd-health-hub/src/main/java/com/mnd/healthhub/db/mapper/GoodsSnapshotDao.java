package com.mnd.healthhub.db.mapper;

import com.mnd.healthhub.db.entity.GoodsSnapshotEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
// 注意导包。
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository // 纳入IoC容器的管理。
@RequiredArgsConstructor
public class GoodsSnapshotDao {

    // Spring Data框架提供的，可以直接注入。
    // 和 RedisTemplate/JdbcTemplate 差不多。
    private final MongoTemplate mongoTemplate;

    /**
     * 根据商品的 md5Hash 值，从mongodb的 mnd-health数据库的 goods_snapshot 集合中查找对应的文档。
     *
     * @param md5Hash 商品的md5哈希值。
     * @return null表示不存在哈希值对应的商品快照，如果商品快照存在的话，返回商品快照这个文档的“_id”
     */
    public String hasGoodsSnapshot(String md5Hash) {
        // 创建一个Query对象，封装查询条件。
        Query query = new Query(Criteria.where("md5Hash").is(md5Hash)).limit(1);
        // 执行查询（执行查找操作）：返回一个文档对象（一个文档就是一条记录）
        GoodsSnapshotEntity entity = mongoTemplate.findOne(query, GoodsSnapshotEntity.class);
        // 如果entity不是null，说明商品快照存在，快照存在则返回文档的主键“_id”
        return entity != null ? entity.get_id() : null;
    }

    /**
     * 保存或更新商品快照信息
     *
     * @param entity 商品快照对象
     * @return 返回保存的商品快照的 _id （文档的主键值）
     */
    public String insert(GoodsSnapshotEntity entity) {
        // save方法两个作用：一个是保存，一个是更新。（insert/update）
        // 什么时候保存？如果 _id 属性值是null：保存操作。
        // 什么时候更新？如果 _id 属性是不是null：更新操作。
        return mongoTemplate.save(entity).get_id();
    }

    /**
     * 根据快照id获取商品快照
     *
     * @param snapshotId 快照id
     * @return 商品快照信息
     */
    public GoodsSnapshotEntity getBySnapshotId(String snapshotId) {
        return mongoTemplate.findById(snapshotId, GoodsSnapshotEntity.class);
    }

    /**
     * 根据商品快照ID获取所有的体检项，并且体检项是适合sex字段数据。
     * @param snapshotId 商品快照ID
     * @param sex 男、女
     * @return 体检项目列表
     */
    public List<Map> searchCheckup(String snapshotId, String sex) {
        // 在MongoDB当中进行复杂查询，需要创建MongoDB的聚合管道。
        // MongoDB的聚合管道的作用：用于处理复杂查询。
        Aggregation aggregation = Aggregation.newAggregation(
                // 第一阶段：投影 - 查询结果只保留 examItems字段，其他字段不需要
                // 当然，我们也是需要 _id 这个字段的，但是由于 _id 字段是主键，主键会自动投影，不需要指定。
                Aggregation.project("examItems"), // 投影的目的是拿到我们需要的我们关心的字段。（现在只关心：_id，examItems）

                // 第二阶段：展开 - 将 examItems数组拆分为多个独立的文档
                // 展开前：{ examItems: [{项目1},{项目2},{项目3}] }
                // 展开后：[{examItems: {项目1}},{examItems: {项目2}},{examItems: {项目3}}]
                Aggregation.unwind("$examItems"), // $ 作用：引用的意思，表示引用examItems字段。

                // 第三阶段：匹配
                Aggregation.match(Criteria.where("_id").is(snapshotId).and("examItems.sex").in("无", sex))
        );

        // 执行聚合查询
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "goods_snapshot", HashMap.class);

        // 遍历结果集，将以上的查询结果转换为我们需要的体检项目数据。
        List<Map> list = new ArrayList<>();
        // 通过 results的getMappedResults()方法获取最终的结果。
        // 最终的结果是：List<HashMap>
        // List<HashMap> 是个什么结构？
        // [{examItems: {项目1}},{examItems: {项目2},{examItems: {项目3}]
        // 我们最终想要的效果: [{项目1},{项目2},{项目3}]
        results.getMappedResults().forEach(one -> {
            HashMap map = (HashMap)one.get("examItems");
            list.add(map);
        });

        return list;
    }
}






















