package com.tzp.LifeCycle.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleDataTable;
import com.tzp.LifeCycle.mapper.LifeCycleDataTableMapper;
import com.tzp.LifeCycle.service.LifeCycleDataTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author kangxvdong
 */
@Slf4j
@Service
public class LifeCycleDataTableServiceImpl implements LifeCycleDataTableService {

    @Autowired
    private LifeCycleDataTableMapper lifeCycleDataTableMapper;
    
    /**
     * 创建一个动态表格
     *
     * @param leCyDaTa 创建使用的数据对象
     * @return 返回创建成功的条数
     */
    @Override
    public Integer createTable(LifeCycleDataTable leCyDaTa) {
        String fieldsToJson = JSON.toJSONString(leCyDaTa.getAllFields());
        leCyDaTa.setAllFields(fieldsToJson);
        return lifeCycleDataTableMapper.insert(leCyDaTa);
    }

    /**
     * 删除一个动态表
     *
     * @param leCyDaTa 删除使用的数据对象
     * @return 返回删除成功的条数
     */
    @Override
    public Integer deleteTableById(LifeCycleDataTable leCyDaTa) {
        return lifeCycleDataTableMapper.deleteById(leCyDaTa);
    }

    /**
     * 更新一个动态表数据
     *
     * @param leCyDaTa 更新使用的数据对象
     * @return 返回更新成功的条数
     */
    @Override
    public Integer updateTableById(LifeCycleDataTable leCyDaTa) {
        return lifeCycleDataTableMapper.updateById(leCyDaTa);
    }

    /**
     * 根据索引或者表名查询一个动态表，设计详情
     *
     * @param index 表名（索引名）
     * @return 返回详情数据
     */
    @Override
    public LifeCycleDataTable queryByIndex(String index) {
        return lifeCycleDataTableMapper.selectById(index);
    }

    /**
     * 分页查询
     *
     * @param dto 查询方法
     * @return 返回分页查询结果
     */
    @Override
    public Page<LifeCycleDataTable> queryList(DataBaseQueryDto dto) {
        Page<LifeCycleDataTable> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<LifeCycleDataTable> wrapper = new QueryWrapper<>(null);
        // 拼接显示字段的方法
        wrapper.select("distinct *");
        if ( dto.getQueryAllEqualFields() != null && !dto.getQueryAllEqualFields().isEmpty() ) {
            wrapper.allEq(dto.getQueryAllEqualFields());
        }
        return lifeCycleDataTableMapper.selectPage(page, wrapper);
    }
    
}