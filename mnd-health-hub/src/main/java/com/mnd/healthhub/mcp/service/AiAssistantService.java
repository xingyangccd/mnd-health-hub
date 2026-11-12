package com.mnd.healthhub.mcp.service;

/**
 * AI助手服务接口
 */
public interface AiAssistantService {


    /**
     * 与AI对话
     *
     * @param message        用户消息
     * @param token          当前用户登录之后的token（用于MCP工具鉴权）
     * @param conversationId 会话ID
     * @return AI的回复内容
     */
    String chat(String message, String token, String conversationId);
}
