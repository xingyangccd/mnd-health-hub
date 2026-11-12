package com.mnd.healthhub.rag.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.rag.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mis/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识库管理")
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/reload")
    @SaCheckPermission(value = {"ROOT", "KNOWLEDGE:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "重新加载知识库")
    public R reload() {
        int count = knowledgeBaseService.loadDocuments("./knowledge");
        return R.ok("知识库加载完成").put("chunks", count);
    }

    @PostMapping("/add")
    @SaCheckPermission(value = {"ROOT", "KNOWLEDGE:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "添加知识文档")
    public R add(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String source = body.get("source");
        knowledgeBaseService.addDocument(content, source);
        return R.ok("添加成功");
    }
}