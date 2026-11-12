package com.mnd.healthhub.mcp.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.mnd.healthhub.common.R;
import com.mnd.healthhub.config.satoken.StpCustomerUtil;
import com.mnd.healthhub.mcp.controller.form.AiChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mnd.healthhub.mcp.service.AiAssistantService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI 助手接口
 */
@RestController
@RequestMapping("/front/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "业务端-AI 助手", description = "AI 助手对话接口")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    @SaCheckLogin(type = StpCustomerUtil.TYPE)
    @Operation(summary = "AI 对话", description = "用户输入消息，AI 自动调用 MCP 工具完成操作并回复")
    public R chat(@RequestBody @Valid AiChatForm form) {
        // 从 Token 中获取当前客户的登录 Token（用于 MCP 工具鉴权）
        String token = StpCustomerUtil.getTokenValue();

        // 如果前端没传 conversationId，就生成一个新的
        String conversationId = form.getConversationId();
        if (!StringUtils.hasText(conversationId)) {
            // 通过UUID生成的 conversationId
            conversationId = "conv_" + UUID.randomUUID().toString().replace("-", "");
        }

        // 调用 Service
        String reply = aiAssistantService.chat(form.getMessage(), token, conversationId);

        // 返回实际使用的 conversationId（不是 form 里的）
        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("conversationId", conversationId);
        return R.ok().put("result", result);
    }
}