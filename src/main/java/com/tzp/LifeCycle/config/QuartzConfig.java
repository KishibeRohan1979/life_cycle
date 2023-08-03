package com.tzp.LifeCycle.config;

import com.tzp.LifeCycle.config.job.QuartzJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时任务，以及一些使用的公共方法
 *
 * @author kangxvdong
 */
@Configuration
public class QuartzConfig {

    @Autowired
    private Scheduler scheduler;

    /**
     * 创建定时任务；指定job的id，毫秒数，和运行的次数来进行定时任务
     *
     * @param jobId 定时任务的id
     * @param delayInMilliseconds 毫秒数
     * @param repeatCount 执行次数
     * @throws SchedulerException 异常
     */
    public void createJobByRepeat(String jobId, long delayInMilliseconds, int repeatCount) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                .withIdentity(jobId, "group")
                .build();

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, "group")
                .startAt(DateBuilder.futureDate((int) delayInMilliseconds, DateBuilder.IntervalUnit.MILLISECOND))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(delayInMilliseconds)
                        // 设置重复次数，如果是repeatCount==0，代表执行1次
                        .withRepeatCount(repeatCount-1)
                )
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    /**
     * 创建定时任务；指定job的id，和提供一个cron表达式
     *
     * @param jobId 定时任务的id
     * @param cron cron表达式
     * @throws SchedulerException 异常
     */
    public void createJobByCron(String jobId, String cron) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                .withIdentity(jobId, "group")
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, "group")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    /**
     * 创建定时任务；指定job的id，和提供一个时间戳，当某个时刻到达时开始执行
     * 这个方法给的时间戳，如果是过去的时间，会立马执行一次
     *
     * @param jobId 定时任务的id
     * @param timestamp 时间戳
     * @throws SchedulerException 异常
     */
    public void createJobAtTimestamp(String jobId, long timestamp) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class)
                .withIdentity(jobId, "group")
                .build();

        Date triggerDate = new Date(timestamp);

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, "group")
                .startAt(triggerDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        // 设置重复次数为0，即只执行一次
                        .withRepeatCount(0)
                )
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    /**
     * 销毁定时任务
     *
     * @param jobId 定时任务的id
     * @throws SchedulerException 异常
     */
    public void deleteJob(String jobId) throws SchedulerException {
        JobKey jobKey = new JobKey(jobId, "group");
        scheduler.deleteJob(jobKey);
    }

    /**
     * 暂停定时任务
     *
     * @param jobId 定时任务的id
     * @throws SchedulerException 异常
     */
    public void pauseJob(String jobId) throws SchedulerException {
        JobKey jobKey = new JobKey(jobId, "group");
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复定时任务
     *
     * @param jobId 定时任务的id
     * @throws SchedulerException 异常
     */
    public void resumeJob(String jobId) throws SchedulerException {
        JobKey jobKey = new JobKey(jobId, "group");
        scheduler.resumeJob(jobKey);
    }

    /**
     * 列出所有的定时任务
     *
     * @return 所有定时任务
     * @throws SchedulerException 异常
     */
    public List<String> listAllJobs() throws SchedulerException {
        List<String> jobList = new ArrayList<>();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);

        for (JobKey jobKey : jobKeys) {
            jobList.add(jobKey.getName());
        }

        return jobList;
    }

}