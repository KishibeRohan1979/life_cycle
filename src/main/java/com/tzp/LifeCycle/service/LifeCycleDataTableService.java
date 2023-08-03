package com.tzp.LifeCycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleDataTable;

/**
 * 用来创建动态表的service，主要实现方式是利用es
 *
 * @author kangxvdong
 */
public interface LifeCycleDataTableService {

    /**
     * 创建一个动态表格
     *
     * @param leCyDaTa 创建使用的数据对象
     * @return 返回创建成功的条数
     */
    Integer createTable(LifeCycleDataTable leCyDaTa);

    /**
     * 删除一个动态表
     *
     * @param leCyDaTa 删除使用的数据对象
     * @return 返回删除成功的条数
     */
    Integer deleteTableById(LifeCycleDataTable leCyDaTa);

    /**
     * 更新一个动态表数据
     *
     * @param leCyDaTa 更新使用的数据对象
     * @return 返回更新成功的条数
     */
    Integer updateTableById(LifeCycleDataTable leCyDaTa);

    /**
     * 根据索引或者表名查询一个动态表，设计详情
     *
     * @param index 表名（索引名）
     * @return 返回详情数据
     */
    LifeCycleDataTable queryByIndex(String index);

    /**
     * 分页查询
     *
     * @param dto 查询方法
     * @return 返回分页查询结果
     */
    Page<LifeCycleDataTable> queryList(DataBaseQueryDto dto);

}
