package com.tzp.LifeCycle.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.tzp.LifeCycle.aop.annotation.DataBaseRequest;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.entity.LifeTest;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.service.LifeTestService;
import com.tzp.LifeCycle.util.MsgUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 测试下的所有接口
 *
 * @author kangxudong
 * @date 2023/07/14 19:01
 */
@RestController
@RequestMapping("/test")
@Api(value = "TestController", tags = "测试下的所有接口")
public class TestController {

    @Autowired
    LifeTestService lifeCycleService;

    @Autowired
    LifeCycleTacticsService lifeCycleTacticsService;

    @ApiOperation("要测试的接口")
    @GetMapping("/testT")
    public MsgUtil<Object> test(String indexName) {
        return MsgUtil.success(indexName);
    }

    @ApiOperation("添加一条数据")
    @PostMapping("/addLifeTest")
    public MsgUtil<Object> addLifeTest(@RequestBody LifeTest lifeTest) {
        Integer createNum = lifeCycleService.createOne(lifeTest);
        if ( createNum == null || createNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

    @ApiOperation("查询一条数据")
    @PostMapping("/queryLifeTest")
    public MsgUtil<Object> queryLifeTest(@RequestBody DataBaseQueryDto dataBaseQueryDto) {
        Page<LifeTest> page = lifeCycleService.queryList(dataBaseQueryDto);
        return MsgUtil.success("查询成功", page);
    }

    /**
     * 修改数据，但是不进行注解操作
     *
     * @param lifeUpdateTactics 数据和策略的一起修改
     * @return 返回信息
     */
    @ApiOperation("修改一条数据")
    @PostMapping("/updateLifeTest")
    public MsgUtil<Object> updateLifeTest(@RequestBody DataBaseUpdateDto<LifeTest> lifeUpdateTactics) {
        // 里面的t引用对象类提出来，更新数据库表的数据
        Integer updateNum = lifeCycleService.updateOne(lifeUpdateTactics.getTObject());
        if ( updateNum == null || updateNum == 0) {
            return MsgUtil.fail();
        }
        // 策略类用来直接添加、更新、删除策略
        // 第一步，查看对应策略是否存在
        // 这一步是利用DataBaseUpdateDto<T>来获取T对应类映射数据库种的表名（使用的是mybatis-plus插件中的方法）
        Class<?> clazz = DataBaseUpdateDto.getClassFromGenericType(lifeUpdateTactics);
        String tableName = SqlHelper.table(clazz).getTableName();
        // 获取表主键列在Java中的映射
        String keyProperty = SqlHelper.table(clazz).getKeyProperty();
        // 获取此次更新中，表主键列的值
        String keyValue = SqlHelper.table(clazz).getPropertyValue(lifeUpdateTactics.getTObject(), keyProperty).toString();
        String schedulerId = tableName + "+" + keyProperty + "+" + keyValue;
        LifeCycleTactics lifeCycleTactics = lifeCycleTacticsService.queryBySchedulerId(schedulerId);
        // 给策略添加一些必要的值
        LifeCycleTactics tactics = lifeUpdateTactics.getLifeCycleTactics();
        tactics.setSchedulerId(schedulerId);
        long nowTime = System.currentTimeMillis();
        tactics.setLastAccessTime(nowTime);
        tactics.setExecutionTime(nowTime + tactics.getDeadline());
        tactics.setAccessAddress(tableName);
        tactics.setDetailedData(JSON.toJSONString(lifeUpdateTactics.getTObject()));
        tactics.setDataObjectType(clazz.getSimpleName());
        // 获取表主键列在数据库表的列名；String keyColumn = SqlHelper.table(LifeTest.class).getKeyColumn()
        tactics.setPrimaryKeyName(SqlHelper.table(clazz).getKeyColumn());
        tactics.setPrimaryKeyType(SqlHelper.table(clazz).getKeyType().getSimpleName());
        tactics.setPrimaryKeyValue(keyValue);
        // 第二步，联立是否存在和周期是否为-1的情况（4种情况）分别做不同的操作
        if (lifeCycleTactics != null && tactics.getDeadline() == -1L) {
            // 存在策略，但是修改的策略周期为-1，策略删除
            lifeCycleTacticsService.deleteOne(lifeCycleTactics);
        } else if (lifeCycleTactics != null && tactics.getDeadline() != -1L) {
            // 存在策略，但是修改的策略周期不为-1，策略更新
            tactics.setTacticsId(lifeCycleTactics.getTacticsId());
            if ( tactics.getTacticsType() == 2 ) {
                tactics.setExecutionTime(lifeCycleTactics.getCreateTime() + tactics.getDeadline());
            }
            System.out.println(tactics.getDeadline());
            lifeCycleTacticsService.updateOne(tactics);
        } else if (lifeCycleTactics == null && tactics.getDeadline() != -1L) {
            // 不存在策略，但是修改的策略周期不为-1，添加策略
            tactics.setCreateTime(nowTime);
            lifeCycleTacticsService.createOne(tactics);
        }
        // 不存在策略，但是修改的策略周期为-1，什么也不做
        return MsgUtil.success("修改成功");
    }

    @ApiOperation("删除一条数据")
    @DataBaseRequest
    @DeleteMapping("/deleteLifeTest")
    public MsgUtil<Object> deleteLifeTest(@RequestBody LifeTest lifeTest) {
        Integer deleteNum = lifeCycleService.deleteOne(lifeTest);
        if ( deleteNum == null || deleteNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

    @ApiOperation("修改生命周期策略")
    @PostMapping("/updateLifeTactics")
    public MsgUtil<Object> updateLifeTactics(@RequestBody LifeCycleTactics lifeCycleTactics) {
        lifeCycleTacticsService.updateOne(lifeCycleTactics);
        return MsgUtil.success("修改成功");
    }
}
