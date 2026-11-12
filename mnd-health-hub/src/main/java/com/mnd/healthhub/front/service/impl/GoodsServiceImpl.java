package com.mnd.healthhub.front.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.db.entity.GoodsSnapshotEntity;
import com.mnd.healthhub.db.mapper.GoodsSnapshotDao;
import com.mnd.healthhub.db.mapper.MedExamPackageMapper;
import com.mnd.healthhub.db.mapper.TradeOrderMapper;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.front.service.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final MedExamPackageMapper medExamPackageMapper;
    private final GoodsSnapshotDao goodsSnapshotDao;
    private final TradeOrderMapper tradeOrderMapper;

    /**
     * @Cacheable(cacheNames = "goods", key = "#id") 作用
     * 1. 执行到 GoodsServiceImpl#getById()方法时，Spring Cache会自动从redis缓存当中查找key：   goods::id
     * 2. 如果找到了这个key，则直接将redis缓存中的数据返回，getById()方法体不执行。
     * 3. 如果没有找到这个key，则执行 getById() 方法体，连接数据库，查询商品信息，
     * spring cache自动将方法的返回值 map 存储到redis缓存中，绑定的key是：goods::id
     */
    @Override
    @Cacheable(cacheNames = "goods", key = "#id")
    public Map<String, Object> getById(Integer id) {
        // 注意：将id和status封装到Map集合中，传递给Mapper
        // status是写死的：status必须等于1，也就是说商品必须是上架的才能预览。
        Map<String, Object> map = medExamPackageMapper.selectById(Map.of("id", id, "status", 1));
        if (CollUtil.isEmpty(map)) {
            throw new HisException("商品不存在", 404);
        }
        // 后端往前端响应数据（后端是json字符串，往前端响应的时候，将json字符串变成json数组）
        String[] jsonFields = {"departmentExam", "labExam", "medicalExam", "otherExam", "tags"};
        for (String field : jsonFields) {
            String dbJsonStr = MapUtil.getStr(map, field);
            if (StrUtil.isNotBlank(dbJsonStr)) {
                map.put(field, JSONUtil.parseArray(dbJsonStr));
            }
        }
        return map;
    }

    @Override
    public Map<Integer, List<Map<String, Object>>> getTop4CategoryIds(Integer[] categoryIds) {
        if (categoryIds == null || categoryIds.length != 3) {
            return new HashMap<>();
        }
        // 12个商品
        List<Map<String, Object>> allList = medExamPackageMapper.selectTop4ByCategoryId(categoryIds[0], categoryIds[1], categoryIds[2]);

        // 把这12个商品分成3块
        // 以下 collect 方法本身就是返回Map集合。
        // groupingBy 是用来指定按照哪个字段进行分组。
        // Collectors.toList() 用来指定每组收集好之后用什么样的集合来存储。
        return allList.stream()
                .collect(Collectors.groupingBy(item -> (Integer) item.get("category_id"), Collectors.toList()));
    }

    @Override
    public PageResult<Map<String, Object>> queryPage(Map<String, Object> params) {
        long total = medExamPackageMapper.selectPageCountForFront(params);
        List<Map<String, Object>> records = new ArrayList<>();
        if (total > 0) {
            records = medExamPackageMapper.selectPageListForFront(params);
        }
        return PageResult.of(records, total);
    }

    @Override
    public Map<String, Object> getBySnapshotId(String snapshotId, Integer customerId) {

        // TODO 业务端用户在使用这个功能的时候，必须提供一个customerId，因为系统要保证当前客户只能看自己的商品快照，
        //  不能说客户只要有一个快照id就可以随便看别人的商品快照。判断逻辑是只要该客户拥有该快照id就可以查看，后面实现,
        //  mis端的用户不需要限制，如果mis端调用该方法，则customerId提供一个null即可。
        if(customerId != null){
            // 这是业务端的用户
            // 根据 snapshot_id 和 customer_id去订单表中查询数据，如果能够查询到，这说明用户有权限查看该商品快照
            long count = tradeOrderMapper.findByCustomerIdAndSnapshotId(customerId, snapshotId);
            if(count == 0){
                throw new HisException("对不起，您没有这个权限查看该商品快照", 400);
            }
        }

        GoodsSnapshotEntity goodsSnapshot = goodsSnapshotDao.getBySnapshotId(snapshotId);
        return BeanUtil.beanToMap(goodsSnapshot);
    }
}




















