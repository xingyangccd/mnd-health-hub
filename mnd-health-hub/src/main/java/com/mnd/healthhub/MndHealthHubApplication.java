package com.mnd.healthhub;

import com.mnd.healthhub.async.AsyncInitWork;
import com.mnd.healthhub.rag.service.KnowledgeBaseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.mnd.healthhub.db.mapper")
@ServletComponentScan
@EnableAsync
@EnableCaching
@EnableScheduling
public class MndHealthHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MndHealthHubApplication.class, args);
        System.out.println(1);
        System.out.println(2);
    }

    // 入口类中注入负责执行初始化任务的bean
    //@Autowired
    @Resource
    private AsyncInitWork asyncInitWork;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    // 该注解作用：MndHealthHubApplication Bean初始化完成之后，自动调用 init() 方法。
    // 为什么要在 MndHealthHubApplication Bean初始化完成之后，原因是：该bean初始化完之后数据库、Redis等一切都做好准备了。
    @PostConstruct
    public void init() {
        asyncInitWork.initWork();

        // 加载RAG知识库
        knowledgeBaseService.loadDocuments("./knowledge");
    }

}
