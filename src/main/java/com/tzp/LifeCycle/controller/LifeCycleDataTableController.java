package com.tzp.LifeCycle.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.aop.annotation.DataBaseAccess;
import com.tzp.LifeCycle.aop.annotation.EsAccess;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.dto.EsDto;
import com.tzp.LifeCycle.dto.EsQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleDataTable;
import com.tzp.LifeCycle.enums.DataAccessType;
import com.tzp.LifeCycle.service.EsDocumentService;
import com.tzp.LifeCycle.service.EsIndexService;
import com.tzp.LifeCycle.service.LifeCycleDataTableService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import com.tzp.LifeCycle.util.MsgUtil;
import com.tzp.LifeCycle.util.SnowFlakeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.action.bulk.BulkResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private EsIndexService esIndexService;

    @Autowired
    private EsDocumentService<Object> esDocumentService;

    @ApiOperation("新增动态表格（表数据添加一条数据）")
    @PostMapping("/addLifeDataTable")
    public MsgUtil<Object> addLifeDataTable(@RequestBody LifeCycleDataTable dataTable) {
        // 检查
        Map<String, Object> fields = JSON.parseObject(JSON.toJSONString(dataTable.getAllFields()));
        if ( !fields.containsKey(dataTable.getPrimaryKeyName()) ){
            return MsgUtil.fail("失败：表设计中不包含该主键");
        }
        // 先向数据库添加这个详细设计内容
        Integer createNum = lifeCycleDataTableService.createTable(dataTable);
        if ( createNum == null || createNum == 0) {
            return MsgUtil.fail("添加设计失败");
        }
        // 再添加索引
        boolean createResult = esIndexService.createIndex(dataTable.getIndexName());
        if (!createResult) {
            return MsgUtil.fail("添加索引失败");
        }
        return MsgUtil.success();
    }

    @ApiOperation("设计动态表格（表数据查询一条数据）")
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
    @ApiOperation("修改动态表格（表数据修改一条数据）")
    @DataBaseAccess(accessType = DataAccessType.UPDATE)
    @PostMapping("/updateLifeDataTable")
    public MsgUtil<Object> updateLifeDataTable(@RequestBody DataBaseUpdateDto<LifeCycleDataTable> lifeUpdateTactics) {
        // 里面的t引用对象类提出来，更新数据库表的数据
        Integer updateNum = lifeCycleDataTableService.updateTableById(lifeUpdateTactics.getTObject());
        if ( updateNum == null || updateNum == 0) {
            return MsgUtil.fail("修改设计失败");
        }
        return MsgUtil.success("修改成功");
    }

    @ApiOperation("删除动态表格（表数据删除一条数据）")
    @DataBaseAccess(accessType = DataAccessType.DELETE)
    @DeleteMapping("/deleteLifeDataTable")
    public MsgUtil<Object> deleteLifeDataTable(@RequestBody DataBaseUpdateDto<LifeCycleDataTable> lifeUpdateTactics) {
        // 先删除es索引
        boolean deleteResult = esIndexService.deleteIndex(lifeUpdateTactics.getTObject().getIndexName());
        if (!deleteResult) {
            return MsgUtil.fail("删除索引失败");
        }
        // 再删除数据库中关于对应表的详细设计
        Integer deleteNum = lifeCycleDataTableService.deleteTableById(lifeUpdateTactics.getTObject());
        if ( deleteNum == null || deleteNum == 0) {
            return MsgUtil.fail("删除设计失败");
        }
        return MsgUtil.success();
    }

    @ApiOperation("添加一条数据给ES")
    @PostMapping("/addLifeTestByEs")
    public MsgUtil<LifeCycleDataTable> addLifeTestByEs(@RequestBody EsDto dto) {
        MsgUtil<LifeCycleDataTable> msgUtil = check(dto);
        if ( !msgUtil.getFlag() ) {
            return msgUtil;
        }
        LifeCycleDataTable table = msgUtil.getData();
        // 第三步，看设计表中的主键是什么（两种解决，1前端直接提供，2后端查数据库data_table表）这里采取了2
        try {
            SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil(1, 1);
            List<Map<String, Object>> addList = new ArrayList<>();
            for (Map<String, Object> map : dto.getDataList()) {
                map.put(table.getPrimaryKeyName(), snowFlakeUtil.nextIdByString());
                addList.add(map);
            }
            BulkResponse create = esDocumentService.batchCreateByCustomizationId(table.getIndexName(), addList, table.getPrimaryKeyName());
            if (create.hasFailures()) {
                return MsgUtil.success("添加成功！但是存在部分文档添加失败。");
            } else {
                return MsgUtil.success("添加成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail();
        }
    }

    @ApiOperation("查询一条数据给ES")
    @PostMapping("/queryLifeTestByEs")
    public MsgUtil<Object> queryLifeTestByEs(@RequestBody EsQueryDto<Object> esQueryDto) {
        try {
            Map<String, Object> map = esDocumentService.searchByQueryObject(esQueryDto);
            return MsgUtil.success("查询成功", map);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail();
        }
    }

    /**
     * 修改数据，但是不进行注解操作
     *
     * @param dto 数据和策略的一起修改
     * @return 返回信息
     */
    @ApiOperation("修改一条数据给ES")
    @EsAccess(accessType = DataAccessType.UPDATE)
    @PostMapping("/updateLifeTestByEs")
    public MsgUtil<LifeCycleDataTable> updateLifeTestByEs(@RequestBody EsDto dto) {
        MsgUtil<LifeCycleDataTable> msgUtil = check(dto);
        if ( !msgUtil.getFlag() ) {
            return msgUtil;
        }
        LifeCycleDataTable table = msgUtil.getData();
        boolean allUpdateSuccess;
        try {
            allUpdateSuccess = esDocumentService.batchUpdateByIdMap(table.getIndexName(), dto.getDataList(), table.getPrimaryKeyName());
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail("修改失败！");
        }
        if (allUpdateSuccess) {
            return MsgUtil.success("修改成功");
        } else {
            return MsgUtil.success("修改成功，但部分修改失败");
        }
    }

    @ApiOperation("删除一条数据给ES")
    @EsAccess(accessType = DataAccessType.DELETE)
    @DeleteMapping("/deleteLifeTestByEs")
    public MsgUtil<LifeCycleDataTable> deleteLifeTestByEs(@RequestBody EsDto dto) {
        MsgUtil<LifeCycleDataTable> msgUtil = check(dto);
        if ( !msgUtil.getFlag() ) {
            return msgUtil;
        }
        LifeCycleDataTable table = msgUtil.getData();
        List<Map<String, Object>> maps = dto.getDataList();
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            ids.add(map.get(table.getPrimaryKeyName()).toString());
        }
        try {
            esDocumentService.batchDeleteByIds(dto.getIndexName(), ids);
        } catch (Exception e) {
            e.printStackTrace();
            return MsgUtil.fail();
        }
        return MsgUtil.success();
    }

    private MsgUtil<LifeCycleDataTable> check(EsDto dto) {
        // 第一步，确认是否有名为 indexName 的索引，存在再添加文档
        boolean exists = esIndexService.indexExists(dto.getIndexName());
        if (!exists) {
            return MsgUtil.fail("名为" + dto.getIndexName() + "的索引不存在！");
        }
        // 第二步，确认输入要插入的内容与数据库表设计是否一致
        LifeCycleDataTable table = lifeCycleDataTableService.queryByIndex(dto.getIndexName());
        if (table == null) {
            return MsgUtil.fail("名为" + dto.getIndexName() + "的设计表不存在！");
        }
        Map<String, Object> tableMap = JSON.parseObject(table.getAllFields().toString());
        // 确认输入要插入的内容与数据库表设计是否一致的方法，true为一致，false不一致
        if ( !LifeStringUtil.checkKeysExistence(tableMap, dto.getDataList().get(0)) ) {
            return MsgUtil.fail("输入数据格式与表设计不一致！");
        }
        return MsgUtil.success("校验成功。", table);
    }

}
