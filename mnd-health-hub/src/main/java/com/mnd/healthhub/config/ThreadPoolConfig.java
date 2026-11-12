package com.mnd.healthhub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * <p>使用方式：
 * <pre>
 * // 通用异步任务
 * &#64;Async("taskExecutor")
 * public void sendLog() { ... }
 *
 * // IO密集型任务（数据库批量查询、文件读写、远程调用）
 * &#64;Async("ioTaskExecutor")
 * public void batchQuery() { ... }
 *
 * // CPU密集型任务（加密解密、大数据排序、图片处理）
 * &#64;Async("cpuTaskExecutor")
 * public void encryptData() { ... }
 * </pre>
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

    /**
     * 默认线程池 - 用于通用异步任务
     * 适用于大部分业务场景，如发送通知、记录日志等
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");
        // 拒绝策略：调用者运行，保证任务不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("默认线程池初始化完成: core={}, max={}, queue={}", 10, 20, 200);
        return executor;
    }

    /**
     * IO密集型线程池 - 用于大量IO操作的任务
     * 适用于：数据库批量操作、文件读写、远程接口调用、导出Excel等
     *
     * <p>配置公式：核心线程数 = CPU核数 * 2，最大线程数 = CPU核数 * 4
     * IO密集场景CPU大多在等待，可多开线程提高吞吐量
     */
    @Bean("ioTaskExecutor")
    public ThreadPoolTaskExecutor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int coreSize = Math.max(cpuCores * 2, 4);   // 至少4个
        int maxSize = Math.max(cpuCores * 4, 8);    // 至少8个

        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-io-");
        // 拒绝策略：调用者运行，避免数据丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("IO线程池初始化完成: core={}, max={}, queue={}", coreSize, maxSize, 500);
        return executor;
    }

    /**
     * CPU密集型线程池 - 用于大量计算的任务
     * 适用于：加解密、数据聚合计算、复杂校验等
     *
     * <p>配置公式：核心线程数 = CPU核数 + 1
     * CPU密集场景线程切换成本高，线程数不宜过多
     */
    @Bean("cpuTaskExecutor")
    public ThreadPoolTaskExecutor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int size = cpuCores + 1;

        executor.setCorePoolSize(size);
        executor.setMaxPoolSize(size);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("async-cpu-");
        // 拒绝策略：直接抛异常，让上游感知问题
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("CPU线程池初始化完成: core={}, max={}, queue={}", size, size, 200);
        return executor;
    }
}