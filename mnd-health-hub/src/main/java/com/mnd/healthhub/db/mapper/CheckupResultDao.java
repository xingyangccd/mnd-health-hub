package com.mnd.healthhub.db.mapper;

import cn.hutool.core.collection.CollUtil;
import com.mnd.healthhub.db.entity.CheckupResultEntity;
import com.mnd.healthhub.exception.HisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mongodb中体检结果数据的增删改查
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CheckupResultDao {

    private final MongoTemplate mongoTemplate;

    /**
     * 保存体检结果（在体检人签到时执行，先保存一个空壳，_id、uuid、checkup这三个字段是有值的，place、result两个没有值。）
     *
     * @param uuid    预约编号（appointmentNo）
     * @param checkup 具体的体检项
     * @return true表示保存成功，false表示失败。
     */
    public boolean insert(String uuid, List<Map> checkup) {
        CheckupResultEntity entity = new CheckupResultEntity();
        entity.setUuid(uuid);
        entity.setCheckup(checkup);
        entity.setPlace(new ArrayList<>());
        entity.setResult(new ArrayList<>());
        // mongodb保存成功之后，会自动将主键 _id 的值赋值给 entity的_id属性
        entity = mongoTemplate.insert(entity);
        return entity.get_id() != null;
    }

    /**
     * 根据预约编号查询体检结果的主键 _id
     *
     * @param appointmentNo 预约编号
     * @return _id 文档对应的主键值
     */
    public String findIdByAppointNo(String appointmentNo) {
        Criteria criteria = Criteria.where("uuid").is(appointmentNo);
        Query query = new Query(criteria);
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);
        return entity.get_id();
    }

    /**
     * 根据 体检编号+科室 查询该体检人在该科室中需要做的体检项目。
     *
     * @param uuid  体检编号
     * @param place 科室
     * @return 体检项目列表
     */
    public List<Map> searchCheckupByPlace(String uuid, String place) {
        // 创建聚合管道
        Aggregation aggregation = Aggregation.newAggregation(
                // 第一阶段：投影
                // 投影的作用是：我接下来的查询只关心两个字段：checkup、uuid，其他字段不需要
                // 展开前：{uuid: "xxx", checkup:[....]}
                Aggregation.project("checkup", "uuid"), // 三个字段投影的：_id 也在，主键自动投影。
                // 第二阶段：展开
                // 展开后：[{_id: "yyyy", uuid: "xxx", checkup: {项目1}}, {_id: "yyyy", uuid: "xxx", checkup: {项目2}}, {_id: "yyyy", uuid: "xxx", checkup: {项目3}}]
                Aggregation.unwind("$checkup"),
                // 第三阶段：匹配
                Aggregation.match(Criteria.where("uuid").is(uuid).and("checkup.place").is(place))
        );

        // 执行查询
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "checkup_result", Map.class);

        List<Map> mappedResults = results.getMappedResults();

        /*
        以上执行完的数据是这样的：
            [
              {
                  _id: "6511425827c25d66503d6aa0",
                  uuid: "F7ADC0D6C1AB4E18A5B7C72005E72AA5",
                  checkup: {
                      template: "模板1",
                      item: "裸眼视力(左)",
                      code: "无",
                      sex: "无",
                      name: "眼科检查",
                      place: "眼科",
                      type: "录入",
                      value: "无"
                  }
              },
              {
                  _id: "6511425827c25d66503d6aa0",
                  uuid: "F7ADC0D6C1AB4E18A5B7C72005E72AA5",
                  checkup: {
                      template: "模板1",
                      item: "裂隙灯检查",
                      code: "无",
                      sex: "无",
                      name: "眼科检查",
                      place: "眼科",
                      type: "录入",
                      value: "未见异常#其他"
                  }
              }
            ]
         */

        /*
            我们需要把以上的查询结果转换成这个格式
                [
                    {
                        template: "模板1",
                        item: "裸眼视力(左)",
                        code: "无",
                        sex: "无",
                        name: "眼科检查",
                        place: "眼科",
                        type: "录入",
                        value: "无"
                    },
                    {
                        template: "模板1",
                        item: "裂隙灯检查",
                        code: "无",
                        sex: "无",
                        name: "眼科检查",
                        place: "眼科",
                        type: "录入",
                        value: "未见异常#其他"
                    }
                ]
         */
        List<Map> list = new ArrayList<>();
        mappedResults.forEach(one -> {
            HashMap map = (HashMap) one.get("checkup");
            list.add(map);
        });
        return list;
    }

    /**
     * 判断该体检人在该科室是否已经有体检结果了。
     *
     * @param uuid  体检编号（代表了体检人）
     * @param place 科室
     * @return true表示该体检人在当前科室已经有体检结果了。false表示没有。
     */
    public boolean hasCheckupResults(String uuid, String place) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.project("result", "uuid"),
                Aggregation.unwind("$result"),
                Aggregation.match(Criteria.where("uuid").is(uuid).and("result.place").is(place))
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "checkup_result", Map.class);
        // 只要不为空，就代表有体检结果。
        return CollUtil.isNotEmpty(results.getMappedResults());
    }

    /**
     * 保存/更新体检结果
     *
     * @param uuid  体检编号（体检人）
     * @param place 科室
     * @param map   该科室的体检结果
     */
    public void addResult(String uuid, String place, Map map) {
        log.info("添加/更新体检结果，uuid={}，place={}", uuid, place);

        // 创建查询条件（根据uuid查询）
        Query query = new Query(Criteria.where("uuid").is(uuid));
        CheckupResultEntity entity = mongoTemplate.findOne(query, CheckupResultEntity.class);

        if (entity == null) {
            log.error("未找到体检记录，uuid={}", uuid);
            throw new HisException("未找到体检记录");
        }

        // 将对象中的place和result取出
        List<String> placeList = entity.getPlace();
        List<Map> resultList = entity.getResult();

        // 通过 place 在数组中的下标找到对应result中的下标。
        int index = placeList.indexOf(place);
        if (index >= 0) {
            // 这个科室对应的体检结果已经存在了。
            resultList.set(index, map);
            log.info("更新体检结果，uuid={},place={}", uuid, place);
        } else {
            // 这个科室对应的体检结果还不存在。
            placeList.add(place);
            resultList.add(map);
            log.info("新增体检结果，uuid={},place={}", uuid, place);
        }

        // 两个作用：保存或更新。主要依据是 _id 的值是否已存在。
        mongoTemplate.save(entity);
    }

    /**
     * 根据主键ID获取体检结果（包含体检项目+体检结果）
     *
     * @param id 主键ID
     * @return 体检结果
     */
    public CheckupResultEntity findById(String id) {
        return mongoTemplate.findById(id, CheckupResultEntity.class);
    }

}





















