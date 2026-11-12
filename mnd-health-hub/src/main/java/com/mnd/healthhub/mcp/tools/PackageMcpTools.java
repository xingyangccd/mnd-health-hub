package com.mnd.healthhub.mcp.tools;

import com.mnd.healthhub.common.PageResult;
import com.mnd.healthhub.front.service.GoodsService;
import com.mnd.healthhub.mcp.config.McpAuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检套餐 MCP 工具
 * <p>
 * 说明：所有需要登录的工具通过 McpAuthUtil 从当前对话上下文中解析客户ID，
 * 不再从方法参数接收 customerId 或 token，避免暴露给大模型导致泄露或伪造。
 */
@Service
@RequiredArgsConstructor
public class PackageMcpTools {

    private final GoodsService goodsService;

    /**
     * 根据套餐ID查询套餐详情（公开接口，无需登录）
     */
    @Tool(description = "根据套餐ID查询体检套餐的完整详细信息，包含体检项目、价格、图片等。此接口为公开数据，无需登录。")
    public Map<String, Object> getPackageDetail(
            @ToolParam(description = "体检套餐的ID，比如 1、2、3", required = true)
            Integer id) {
        return goodsService.getById(id);
    }

    /**
     * 分页查询在售套餐列表（公开接口，无需登录）
     */
    @Tool(description = "分页查询所有上架的体检套餐列表，支持按关键词、套餐类型筛选。此接口为公开数据，无需登录。")
    public Map<String, Object> listPackages(
            @ToolParam(description = "页码，从1开始", required = true)
            Integer pageNo,
            @ToolParam(description = "每页大小，建议10-20", required = true)
            Integer pageSize,
            @ToolParam(description = "搜索关键词，如'入职体检'，可选", required = false)
            String keyword,
            @ToolParam(description = "套餐类型：父母体检/入职体检/职场白领/个人高端/中青年体检，可选", required = false)
            String packageType) {

        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("startIndex", (pageNo - 1) * pageSize);
        if (keyword != null && !keyword.isEmpty()) {
            params.put("keyword", keyword);
        }
        if (packageType != null && !packageType.isEmpty()) {
            params.put("packageType", packageType);
        }

        PageResult<Map<String, Object>> pageResult = goodsService.queryPage(params);
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageResult.getTotal());
        result.put("list", pageResult.getRecords());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 获取各分类销量TOP4套餐（公开接口，无需登录）
     */
    @Tool(description = "获取每个分类下销量排名前4的热门体检套餐，用于首页推荐展示。此接口为公开数据，无需登录。")
    public Map<Integer, List<Map<String, Object>>> getTop4Packages(
            @ToolParam(description = "分类ID数组，比如 [1, 2, 3] 分别对应活动专区、热卖套餐、新品推荐", required = true)
            Integer[] categoryIds) {
        return goodsService.getTop4CategoryIds(categoryIds);
    }

    /**
     * 根据快照ID查询套餐快照（需要登录，校验归属权）
     */
    @Tool(description = "根据商品快照ID查询套餐在购买时的完整快照信息，包含所有体检项目明细。需要登录，且只允许查询当前用户自己拥有的快照。")
    public Map<String, Object> getPackageSnapshot(
            @ToolParam(description = "商品快照ID，一般是24位十六进制字符串", required = true)
            String snapshotId) {
        int customerId = McpAuthUtil.requireCustomerId();
        return goodsService.getBySnapshotId(snapshotId, customerId);
    }
}