package com.mnd.healthhub.mcp.tools;

import cn.hutool.json.JSONObject;
import com.mnd.healthhub.app.service.FlowRegulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流量调控 MCP 工具
 * 提供科室推荐、排队信息查询等能力（公开数据，无需登录）
 */
@Service
@RequiredArgsConstructor
public class FlowRegulationMcpTools {

    private final FlowRegulationService flowRegulationService;

    /**
     * 获取推荐科室列表（公开接口，无需登录）
     */
    @Tool(description = "获取当前系统推荐的就诊科室列表，按流量调控策略（自动按权重/手动按优先级）排序，用于导诊分流。此接口为公开数据，无需登录。")
    public List<JSONObject> getRecommendedDepartments() {
        return flowRegulationService.getRecommendations();
    }

    /**
     * 获取科室详情（公开接口，无需登录）
     */
    @Tool(description = "根据科室名称查询科室的流量调控详情，包含当前排队人数、最大容量、权重等。此接口为公开数据，无需登录。")
    public String getDepartmentDetail(
            @ToolParam(description = "科室名称，如'眼科'、'内科'", required = true)
            String departmentName) {
        List<JSONObject> list = flowRegulationService.getRecommendations();
        for (JSONObject item : list) {
            if (departmentName.equals(item.getStr("departmentName"))) {
                return item.toStringPretty();
            }
        }
        return "未找到科室: " + departmentName;
    }
}