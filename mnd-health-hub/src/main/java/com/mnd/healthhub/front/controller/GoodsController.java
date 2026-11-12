package com.mnd.healthhub.front.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.front.controller.form.GetGoodsSnapshotForm;
import com.mnd.healthhub.front.controller.form.GetTop4ByCategoryIdsForm;
import com.mnd.healthhub.front.controller.form.PageQueryGoodsForm;
import com.mnd.healthhub.front.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("frontGoodsController")
@RequestMapping("/front/goods")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "商品管理")
public class GoodsController {
    private final GoodsService goodsService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询商品")
    public R getById(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable
            @NotNull(message = "id不能为空")
            @Min(value = 1, message = "id不能小于1")
            Integer id) {
        Map<String, Object> goods = goodsService.getById(id);
        return R.ok().put("goods", goods);
    }

    // 使用post请求（按照正常来说查询应该使用get请求），原因是：因为要提交的是数组，post处理更加方便。（国内也有很多团队直接这样写。）
    @PostMapping("/top4")
    @Operation(summary = "获取各分类Top4套餐", description = "根据分类ID数组，返回每个分类下销量排名前4的套餐")
    public R top4(@RequestBody @Valid GetTop4ByCategoryIdsForm form) {
        Map<Integer, List<Map<String, Object>>> top4 = goodsService.getTop4CategoryIds(form.getCategoryIds());
        return R.ok().put("top4", top4);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "根据查询条件分页查询套餐列表")
    public R list(@ParameterObject @Valid PageQueryGoodsForm form) {
        Integer pageNo = form.getPageNo();
        Integer pageSize = form.getPageSize();
        Integer startIndex = (pageNo - 1) * pageSize;
        Map<String, Object> map = BeanUtil.beanToMap(form);
        map.put("startIndex", startIndex);
        PageResult<Map<String, Object>> pageResult = goodsService.queryPage(map);
        return R.ok().put("pageResult", pageResult);
    }

    @GetMapping("/mis/snapshot")
    @Operation(summary = "MIS端查询商品快照", description = "根据快照ID查询商品快照信息，MIS端不需要提供customerId")
    @SaCheckPermission(value = {"ROOT", "GOODS:SELECT"}, mode = SaMode.OR)
    public R getBySnapshotIdForMis(@ParameterObject @Valid GetGoodsSnapshotForm form) {
        Map<String, Object> map = goodsService.getBySnapshotId(form.getSnapshotId(), null);
        return R.ok().put("result", map);
    }


    @GetMapping("/snapshot")
    @Operation(summary = "业务端查询商品快照", description = "根据快照ID查询商品快照信息，业务端需要提供customerId")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    public R getBySnapshotIdForFront(@ParameterObject @Valid GetGoodsSnapshotForm form) {
        int customerId = StpCustomerUtil.getLoginIdAsInt();
        Map<String, Object> map = goodsService.getBySnapshotId(form.getSnapshotId(), customerId);
        return R.ok().put("result", map);
    }

}

















