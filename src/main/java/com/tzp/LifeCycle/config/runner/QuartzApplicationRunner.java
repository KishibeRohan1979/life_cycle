package com.tzp.LifeCycle.config.runner;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.config.QuartzConfig;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.mapper.LifeCycleTacticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目在启动的时候，读取数据库的策略，添加定时任务
 *
 * @author kangxudong
 */
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

        while (hasNextPage) {
            // 分页查询数据
            Page<LifeCycleTactics> page = new Page<>(currentPage, pageSize);
            Page<LifeCycleTactics> resultPage = lifeCycleTacticsMapper.selectPage(page, null);

            // 处理当前页的数据
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
    }

}
