package com.mnd.healthhub.async;

import com.mnd.healthhub.mis.service.MedExamReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncCreateReport {

    private final MedExamReportService medExamReportService;

    // 这里使用的是IO密集型的线程池（这个方法提交给线程池，线程池会自动找一个空闲的线程来完成任务）
    @Async("ioTaskExecutor")
    public void createReport(Integer id) {
        // 生成体检报告
        medExamReportService.createReport(id);
    }
}