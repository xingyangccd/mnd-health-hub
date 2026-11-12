package com.mnd.healthhub.mcp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mnd.healthhub.exception.HisException;
import com.mnd.healthhub.mcp.config.McpToolContextHolder;
import com.mnd.healthhub.mcp.service.AiAssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * AI助手服务实现
 * 核心职责：
 * 1. 接收用户消息，调用deepseek大模型生成回复。
 * 2. 大模型可以自主的调用MCP工具（比如：查订单、下单、预约、退款）
 * 3. 维护好多轮对话上下文（同一个会话ID记录历史消息）
 * 4. 把当前用户的Token传递给MCP工具，用于鉴权。
 */
@Slf4j
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    /**
     * 系统提示词
     * <p>
     * 用于给大模型设定角色、职责和工作约束。
     * 提示词质量直接决定 AI 回复的专业度，可按业务持续优化。
     */
    private static final String SYSTEM_PROMPT = """
            你是美年大健康体检中心的 AI 健康助手。
            
            你的职责：
            1. 帮助用户查询体检套餐、订单、预约、体检报告等信息。
            2. 帮助用户完成下单、预约、退款等操作。
            3. 当用户询问体检相关问题时，给出专业、友好的建议。
            
            工作要求：
            1. 回答要简洁、专业、友好，使用中文。
            2. 如果需要调用工具获取数据，先调用工具，再根据结果回答。
            3. 如果用户未登录，涉及个人数据的操作要提示用户先登录。
            4. 不要编造数据，一切以工具返回结果为准。
            5. 涉及金额、日期等关键信息时，务必准确。
            6. 结合之前的对话上下文理解用户的连续追问。
            """;

    /**
     * 1. 线程安全的HashMap（JUC并发面试题的时候再说。）
     * 2. 会话活跃时间记录：key是：conversationId，value是：最后活跃时间戳
     * 3. 用于清理长时间不活跃的会话，避免内存无限增长。
     */
    private final ConcurrentHashMap<String, Long> sessionActivity = new ConcurrentHashMap<>();

    /**
     * 会话的过期时间 30 分钟。
     * 超过这个时间没有新的消息，会话将从 sessionActivity 当中删除。
     */
    private static final long SESSION_TLL_MS = 30 * 60 * 1000L;

    /**
     * 大模型实例
     * 由Spring AI自动配置注入，配置来源：application.yml文件中的 spring.ai.openai.*
     */
    private final ChatModel chatModel;

    /**
     * 对话记忆
     * 当前对话记忆是基于内存实现的，服务器启动之后记忆就丢失了。后续你可以升级成将消息持久化到redis当中。
     * 当重启服务器的时候，对话记忆还在。保留最近20条消息，超出上限时，比较早的历史消息会自动移除。
     */
    private final ChatMemory chatMemory;

    /**
     * MCP工具回调提供者
     * 项目中一共暴露13个MCP工具。供大模型在对话中自动选择调用。（我们程序员不需要干涉，这是大模型自己的任务/MCP客户端自己的任务。）
     */
    private final ToolCallbackProvider toolCallbackProvider;

    // 注入向量数据库（这个Bean之前已经配置了）
    private final VectorStore vectorStore;


    // 通过构造注入，给service对象的属性赋值。
    public AiAssistantServiceImpl(ChatModel chatModel,ToolCallbackProvider toolCallbackProvider, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20) // 保留20条消息。较早消息会被移除。
                .build();
        this.vectorStore = vectorStore;
    }


    @Override
    public String chat(String message, String token, String conversationId) {
        // 1.参数校验
        if(!StringUtils.hasText(message)){
            // 条件成立，表示没有消息。
            throw new HisException("消息内容不能为空", 400);
        }

        // 2. 会话管理
        // 先清理过期的会话，再更新当前会话的活跃时间
        cleanExpiredSessions();
        // 更新当前会话的活跃时间（Map集合中的key重复，value会覆盖。这样会话的最后活跃时间就更新了）
        sessionActivity.put(conversationId, System.currentTimeMillis());

        // 3. 将Token存储到上下文中。 开始与大模型对话：这是起点，先将token保存到当前线程中。MCP工具鉴权要使用
        McpToolContextHolder.setToken(token);

        try {
            // 4. 创建聊天的客户端对象 ChatClient
            ChatClient chatClient = buildChatClient(chatModel);

            // 5. 调用大模型
            String reply = chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
            log.info("AI对话完成：conversationId:{}，replyLength={}", conversationId, StrUtil.isBlank(reply) ? 0 : reply.length());
            return reply;
        } catch (Exception e) {
            log.error("AI对话失败：conversationId: {}", conversationId, e);
            throw new HisException("AI助手暂时无法回答，请稍后再试:" + e.getMessage(), 500);
        } finally {
            // 将token从当前线程中删除
            // Tomcat服务器它是支持多线程的，线程本身是可以复用的。
            // 这里处理好一次会话之后一定要将token从当前线程中删除掉。
            McpToolContextHolder.clear();
        }
    }


    /**
     * 创建ChatClient，聊天客户端对象，专门发送请求给大模型。
     * 1.参数需要将大模型实例传进来。（把deepseek传进来）
     * 2.绑定系统提示词
     * 3.绑定MCP工具，这样大模型才能知道去哪里找暴露的工具。
     * 4.实现多轮对话，保持记忆
     */
    public ChatClient buildChatClient(ChatModel chatModel) {

        // RAG Advisor（RAG增强器）
        // topK(3) ： 返回3个具有相似度的文档块。
        // similarityThreshold(0.5)：相似度低于0.5分时丢弃。
        QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.5).build()).build();

        return ChatClient.builder(chatModel)
                // 绑定系统提示词
                .defaultSystem(SYSTEM_PROMPT)
                // 绑定MCP工具（让大模型可以自主调用）
                .defaultToolCallbacks(toolCallbackProvider)
                // 绑定对话记忆（保持多轮对话上下文）
                // ChatMemory：保存20条消息。
                // MessageChatMemoryAdvisor：把保存的20条消息拼接到请求体当中，发给大模型。
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), ragAdvisor)
                // 构建
                .build();
    }

    /**
     * 清除过期的会话
     */
    private void cleanExpiredSessions() {

        long now = System.currentTimeMillis();

        // 遍历并删除（满足条件就删除）
        sessionActivity.entrySet().removeIf(entry -> {
            // 返回一个true/false(返回true的会被删除)
            boolean expired = now - entry.getValue() > SESSION_TLL_MS;
            if(expired) {
                // 会话过期之后，将会话记录删除。（根据conversationId删除）
                chatMemory.clear(entry.getKey());
            }
            return expired;
        });
    }
}














