package com.tzp.LifeCycle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.aop.annotation.DataBaseDelete;
import com.tzp.LifeCycle.aop.annotation.DataBaseUpdate;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.entity.LifeCycleDataTable;
import com.tzp.LifeCycle.service.LifeCycleDataTableService;
import com.tzp.LifeCycle.util.MsgUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author kangxvdong
 */
@RestController
@RequestMapping("/dataTable")
@Api(value = "LifeCycleDataTableController", tags = "动态表接口")
public class LifeCycleDataTableController {

    @Autowired
    private LifeCycleDataTableService lifeCycleDataTableService;

    @ApiOperation("表数据添加一条数据")
    @PostMapping("/addLifeDataTable")
    public MsgUtil<Object> addLifeDataTable(@RequestBody LifeCycleDataTable dataTable) {
        Integer createNum = lifeCycleDataTableService.createTable(dataTable);
        if ( createNum == null || createNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

    @ApiOperation("表数据查询一条数据")
    @PostMapping("/queryLifeDataTable")
    public MsgUtil<Object> queryLifeDataTable(@RequestBody DataBaseQueryDto dataBaseQueryDto) {
        Page<LifeCycleDataTable> page = lifeCycleDataTableService.queryList(dataBaseQueryDto);
        return MsgUtil.success("查询成功", page);
    }

    /**
     * 修改数据，但是不进行注解操作
     *
     * @param lifeUpdateTactics 数据和策略的一起修改
     * @return 返回信息
     */
    @ApiOperation("表数据修改一条数据")
    @DataBaseUpdate
    @PostMapping("/updateLifeDataTable")
    public MsgUtil<Object> updateLifeDataTable(@RequestBody DataBaseUpdateDto<LifeCycleDataTable> lifeUpdateTactics) {
        // 里面的t引用对象类提出来，更新数据库表的数据
        Integer updateNum = lifeCycleDataTableService.updateTableById(lifeUpdateTactics.getTObject());
        if ( updateNum == null || updateNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success("修改成功");
    }

    @ApiOperation("表数据删除一条数据")
    @DataBaseDelete
    @DeleteMapping("/deleteLifeDataTable")
    public MsgUtil<Object> deleteLifeDataTable(@RequestBody LifeCycleDataTable lifeCycleDataTable) {
        Integer deleteNum = lifeCycleDataTableService.deleteTableById(lifeCycleDataTable);
        if ( deleteNum == null || deleteNum == 0) {
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

}
