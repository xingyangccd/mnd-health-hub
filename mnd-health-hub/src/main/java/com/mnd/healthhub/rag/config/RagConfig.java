package com.mnd.healthhub.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 向量库配置
 * 使用 Spring AI 内置的 SimpleVectorStore（内存版）：
 * - 数据存在 JVM 内存中，服务重启后丢失
 * - 启动时通过 KnowledgeBaseService 重新加载知识库
 */
@Configuration
public class RagConfig {

    /**
     * 内存向量库
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}