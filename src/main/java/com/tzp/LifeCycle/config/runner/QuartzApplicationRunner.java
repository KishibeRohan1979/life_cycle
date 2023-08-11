package com.tzp.LifeCycle.config.runner;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.config.QuartzConfig;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.mapper.LifeCycleTacticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 项目在启动的时候，读取数据库的策略，添加定时任务
 *
 * @author kangxvdong
 */
@Slf4j
@Component
public class QuartzApplicationRunner implements ApplicationRunner {

    @Autowired
    private LifeCycleTacticsMapper lifeCycleTacticsMapper;

    @Autowired
    private QuartzConfig schedulerConfig;

    @Override
    @Async("asyncServiceExecutor")
    public void run(ApplicationArguments args) throws Exception {
        // 每页查询的数据量
        int pageSize = 1000;
        int currentPage = 1;
        boolean hasNextPage = true;

        log.info("开始检查未完成的定时任务");

        while (hasNextPage) {
            // 分页查询数据
            Page<LifeCycleTactics> page = new Page<>(currentPage, pageSize);
            Page<LifeCycleTactics> resultPage = lifeCycleTacticsMapper.selectPage(page, null);

            // 处理当前页的数据
            // 添加定时任务写在循环里，是为了保证稳定，如果数据库在读取时报错，很有可能一个定时任务也添加不了。
            for (LifeCycleTactics lifeCycleTactics : resultPage.getRecords()) {
                // 获取 schedule_id 和 execution_time 的值
                String scheduleId = lifeCycleTactics.getSchedulerId();
                Long executionTime = lifeCycleTactics.getExecutionTime();

                // 调用 创建定时任务 方法
                schedulerConfig.createJobAtTimestamp(scheduleId, executionTime);
            }

            // 判断是否还有下一页
            hasNextPage = resultPage.hasNext();
            currentPage++;
        }

        log.info("检查定时任务完成");
    }

}
