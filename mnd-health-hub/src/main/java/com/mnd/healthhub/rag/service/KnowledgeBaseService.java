package com.mnd.healthhub.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库服务
 * 负责加载文档、切分、向量化、存入向量库
 * 以下这个类仅在服务器启动阶段起作用，在主入口程序中，可以加载文档、切分文档、向量化、存入向量库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    /**
     * 向量库
     * 由 RagConfig 配置的Bean提供的。
     * 负责把文本块的向量值存储到 向量库（Spring AI内存版的向量库，后期你可以换成Redis） 当中。
     * 包括它也可以做后续的检索操作。
     */
    private final VectorStore vectorStore;

    /**
     * 加载所有的知识库文档
     */
    public int loadDocuments(String directoryPath) {
        // 判断目录是否存在的
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("知识库目录不存在：{}", directoryPath);
            return 0;
        }

        // 从目录中找到对应的知识库文件
        File[] files = directory.listFiles(
                (dir, name) -> name.endsWith(".txt")
                        || name.endsWith(".pdf") || name.endsWith(".docx")
                        || name.endsWith(".md"));
        if(files == null || files.length == 0) {
            log.warn("知识库目录为空：{}", directoryPath);
            return 0;
        }

        // 这个List集合存储所有切分之后的文本块（任何一个文本块都是一个独立的Document对象）
        List<Document> allChunks = new ArrayList<>();

        // 创建切分器，在切分器当中指定切分规则。
        /**
         * 文档切分器（Builder 模式）【原则是：不能太大，不能太小】
         * <p>
         * 作用：把长文档切成合适大小的小块，每块单独向量化。
         * <p>
         * 参数说明：
         * - withChunkSize(500)          ：每个块约 500 token
         * - withMinChunkSizeChars(100)  ：块最小字符数 100，太短的块会被合并
         * - withMinChunkLengthToEmbed(5)：最小嵌入长度 5，低于此长度的块不向量化
         * - withMaxNumChunks(1000)      ：最多切 1000 个块，防止失控
         * - withKeepSeparator(true)     ：保留分隔符，避免断句问题
         */
        TokenTextSplitter splitter = new TokenTextSplitter().builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();

        // 遍历所有的文件，进行切分
        for (File file : files) {
            try {
                log.info("加载知识库文件：{}", file.getName());

                // 将文件统一格式：TikaDocumentReader
                TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(file));
                List<Document> documents = reader.get();

                // 把长文档切分为小块
                List<Document> chunks = splitter.apply(documents);

                // 给每个块打上“来源”标记，便于后续溯源。
                for (Document chunk : chunks) {
                    chunk.getMetadata().put("source", file.getName());
                }

                allChunks.addAll(chunks);

                log.info("文件{}切分为{}个块", file.getName(), chunks.size());

            }catch (Exception e) {
                log.error("加载文件失败：{}", file.getName(), e);
            }
        }

        if(!allChunks.isEmpty()) {
            // 将所有的文档块存储到向量数据库当中。
            // 由于我们项目配置了 embedding大模型，因此这里自动先对文档块进行向量化，得出向量值，才会插入到向量数据库当中。
            vectorStore.add(allChunks);
            log.info("知识库加载完成，共{}个文档快", allChunks.size());
        }

        return allChunks.size();
    }

    /**
     * 添加单个文档到向量库中
     */
    public void addDocument(String content, String source) {
        Document doc = new Document(content);
        doc.getMetadata().put("source", source);
        vectorStore.add(List.of(doc));
        log.info("添加文档到知识库：{}", source);
    }
}




















