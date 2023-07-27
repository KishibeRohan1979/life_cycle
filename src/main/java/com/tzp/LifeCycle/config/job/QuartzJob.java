package com.tzp.LifeCycle.config.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author kangxudong
 */
public class QuartzJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            // 在这里写具体执行的内容
            System.out.println("你好，执行定时任务！" + context);
        }
    }