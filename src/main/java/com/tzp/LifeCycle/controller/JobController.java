package com.tzp.LifeCycle.controller;

import com.tzp.LifeCycle.config.QuartzConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 随便做个测试，正式放入项目时，请不要添加这个类，可以选一部分方法
 */
@RestController
@Api(value = "JobController", tags = "定时任务的一些测试接口")
@RequestMapping("/jobs")
public class JobController {

//    private final QuartzConfig schedulerConfig;
//
//    @Autowired
//    public JobController(QuartzConfig schedulerConfig) {
//        this.schedulerConfig = schedulerConfig;
//    }

    @Autowired
    private QuartzConfig schedulerConfig;

    @ApiOperation("根据间隔毫秒数、重复次数创建一个定时任务")
    @PostMapping("/{jobId}")
    public void createJobByRepeat(@PathVariable String jobId, @RequestParam long delayInMilliseconds, @RequestParam int repeatCount) throws SchedulerException {
        schedulerConfig.createJobByRepeat(jobId, delayInMilliseconds, repeatCount);
    }

    @ApiOperation("根据cron表达式创建一个定时任务")
    @PostMapping("/cron/{jobId}")
    public void createJobByCron(@PathVariable String jobId, @RequestParam String cron) throws SchedulerException {
        schedulerConfig.createJobByCron(jobId, cron);
    }

    @ApiOperation("根据具体的时间戳（精确到最后一个毫秒）创建一个定时任务")
    @PostMapping("/timestamp/{jobId}")
    public void createJobAtTimestamp(@PathVariable String jobId, @RequestParam long timestamp) throws SchedulerException {
        schedulerConfig.createJobAtTimestamp(jobId, timestamp);
    }

    @ApiOperation("根据id删除一个定时任务")
    @DeleteMapping("/{jobId}")
    public void deleteJob(@PathVariable String jobId) throws SchedulerException {
        schedulerConfig.deleteJob(jobId);
    }

    @ApiOperation("暂停一个定时任务")
    @PutMapping("/pause/{jobId}")
    public void pauseJob(@PathVariable String jobId) throws SchedulerException {
        schedulerConfig.pauseJob(jobId);
    }

    @ApiOperation("恢复暂停的定时任务")
    @PutMapping("/resume/{jobId}")
    public void resumeJob(@PathVariable String jobId) throws SchedulerException {
        schedulerConfig.resumeJob(jobId);
    }

    @ApiOperation("查看所有的定时任务")
    @GetMapping("/list")
    public List<String> listAllJobs() throws SchedulerException {
        return schedulerConfig.listAllJobs();
    }
}
