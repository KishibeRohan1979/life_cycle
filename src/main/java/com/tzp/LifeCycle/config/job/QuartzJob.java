package com.tzp.LifeCycle.config.job;

import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.mapper.LifeCycleTacticsMapper;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author kangxvdong
 */
@Slf4j
@Component
public class QuartzJob implements Job {

    @Autowired
    private LifeCycleTacticsService lifeCycleTacticsService;

    @Autowired
    private LifeCycleTacticsMapper lifeCycleTacticsMapper;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 在这里写具体执行的内容
        // 获取当前任务的 JobDetail 对象
        JobDetail jobDetail = context.getJobDetail();
        // 获取当前任务的 ID
        JobKey jobKey = jobDetail.getKey();
        String jobId = jobKey.getName();
        log.info("开始执行 " + jobId +" 的定时任务！");
        LifeCycleTactics cycleTactics = lifeCycleTacticsService.queryBySchedulerId(jobId);
        if (cycleTactics != null) {
            String tableName = cycleTactics.getAccessAddress();
            String primaryKeyName = cycleTactics.getPrimaryKeyName();
            String primaryKeyValue = cycleTactics.getPrimaryKeyValue();
            Map<String, Object> map = lifeCycleTacticsMapper.selectByPrimaryKey(tableName, primaryKeyName, primaryKeyValue);
            if (map != null) {
                // 原数据删除
                Integer num = lifeCycleTacticsMapper.deleteByPrimaryKey(tableName, primaryKeyName, primaryKeyValue);
            }
            // 不管值有没有，策略都得删除了
            lifeCycleTacticsService.deleteOne(cycleTactics);
            // 任务执行完毕之后，这个Quartz框架自动删除对应的定时任务，无需我们手动再删除了
        }
    }

    /**
     * 根据字符串（类似：java.lang.String）返回对应类（返回一个String.class）
     *
     * @param className 绝对要全路径，因为还有可能有包里的类，自定义的类等
     * @return 返回对应类
     * @throws ClassNotFoundException 异常
     */
    public static Class<?> getClassFromString(String className) throws ClassNotFoundException {
        // 如果输入字符串为空或者为空白字符串，则返回 null
        if (className == null || className.trim().isEmpty()) {
            return null;
        }
        // 使用 Class.forName 方法来获取 Class 对象
        return Class.forName(className);
    }

}