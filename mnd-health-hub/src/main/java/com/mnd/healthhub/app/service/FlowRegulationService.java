package com.mnd.healthhub.app.service;


import cn.hutool.json.JSONObject;

import java.util.List;

public interface FlowRegulationService {

    /**
     * 获取推荐列表给app端。
     *
     * @return 推荐列表
     */
    List<JSONObject> getRecommendations();
}
