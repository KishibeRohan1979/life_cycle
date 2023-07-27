package com.tzp.LifeCycle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.aop.annotation.DataBaseDelete;
import com.tzp.LifeCycle.aop.annotation.DataBaseUpdate;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.entity.LifeTest;
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
    private LifeTestService lifeCycleService;

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
    @DataBaseUpdate
    @PostMapping("/updateLifeTest")
    public MsgUtil<Object> updateLifeTest(@RequestBody DataBaseUpdateDto<LifeTest> lifeUpdateTactics) {
        // 里面的t引用对象类提出来，更新数据库表的数据
        Integer updateNum = lifeCycleService.updateOne(lifeUpdateTactics.getTObject());
        if ( updateNum == null || updateNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success("修改成功");
    }

    @ApiOperation("删除一条数据")
    @DataBaseDelete
    @DeleteMapping("/deleteLifeTest")
    public MsgUtil<Object> deleteLifeTest(@RequestBody LifeTest lifeTest) {
        Integer deleteNum = lifeCycleService.deleteOne(lifeTest);
        if ( deleteNum == null || deleteNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

}
