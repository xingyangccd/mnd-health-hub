package com.mnd.healthhub.schedule;

import cn.hutool.core.collection.CollUtil;
import com.mnd.healthhub.async.AsyncCreateReport;
import com.mnd.healthhub.db.mapper.MedExamReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MedExamReportSchedule {

    // 调用它来获取前50个
    private final MedExamReportMapper medExamReportMapper;

    // 调用它来生成体检报告
    private final AsyncCreateReport asyncCreateReport;

    //@Scheduled(cron = "0 0,20,40 1,2,3,4,5 * * ?") // 生成环境
    @Scheduled(cron = "0 0/1 * * * ?") // 测试环境：每分钟一次。
    public void createReport(){
        List<Integer> list = medExamReportMapper.select50NonReport();
        if(CollUtil.isEmpty(list)){
            // 任务结束
            return;
        }
        // 异步生成体检报告。（不会一份等一份，是同时的。）
        list.forEach(asyncCreateReport::createReport);
        log.info("体检报告生成{}份", list.size());
    }
}
