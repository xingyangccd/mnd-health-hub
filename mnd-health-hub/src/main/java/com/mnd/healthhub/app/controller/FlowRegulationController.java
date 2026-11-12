package com.mnd.healthhub.app.controller;

import cn.hutool.json.JSONObject;
import com.mnd.healthhub.app.service.FlowRegulationService;
import com.mnd.healthhub.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("appFlowRegulationController")
@RequestMapping("/app/flow-regulation")
@RequiredArgsConstructor
@Tag(name = "APP-流量调控")
public class FlowRegulationController {

    private final FlowRegulationService flowRegulationService;

    @GetMapping("/recommendations")
    @Operation(summary = "获取科室推荐列表")
    public R getRecommendations() {
        List<JSONObject> list = flowRegulationService.getRecommendations();
        return R.ok().put("result", list);
    }
}
