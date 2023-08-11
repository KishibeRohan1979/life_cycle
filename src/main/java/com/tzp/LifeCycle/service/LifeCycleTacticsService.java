package com.tzp.LifeCycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;

import java.io.Serializable;
import java.util.List;

/**
 * @author kangxvdong
 */
public interface LifeCycleTacticsService {

    /**
     * 根据id查询数据
     *
     * @param id 要查询对象的id
     * @return 返回查询的数据
     */
    LifeCycleTactics queryById(Serializable id);

    /**
     * 根据定时id查询数据
     *
     * @param schedulerId 要查询对象的定时id
     * @return 返回查询的数据
     */
    LifeCycleTactics queryBySchedulerId(String schedulerId);

    /**
     * 根据所有定时id查询数据
     *
     * @param schedulerIds 要查询对象的所有定时id
     * @return 返回所有查询的数据
     */
    List<LifeCycleTactics> queryBySchedulerIds(List<String> schedulerIds);

    /**
     * 根据字段条件模糊查询返回分页查询结果
     *
     * @param dto 要查询的查询对象类
     * @return 返回查询的数据集合
     */
    Page<LifeCycleTactics> queryList(DataBaseQueryDto dto);

//    List<String> querySchedulerIdList();

    /**
     * 添加一条数据
     *
     * @param t 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    Integer createOne(LifeCycleTactics t);

    /**
     * 批量添加数据
     *
     * @param list 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    Integer createByList(List<LifeCycleTactics> list);

    /**
     * 根据对象删除一条数据；
     *
     * @param t 删除依据的对象
     * @return 返回删除数据的行数
     */
    Integer deleteOne(LifeCycleTactics t);

    /**
     * 根据 schedulerId 列表批量删除数据；
     *
     * @param schedulerIdList 删除依据的列表
     * @return 返回删除数据的行数
     */
    Integer deleteListBySchedulerId(List<String> schedulerIdList);

    /**
     * 删除where access_address = value的所有行数
     *
     * @param accessAddress 删除accessAddress依据的值
     * @return 返回删除数据的行数
     */
    Integer deleteByAccessAddressValue(String accessAddress);

    // 批量删除

    /**
     * 根据id修改一条数据
     *
     * @param t 准备好修改数据的对象
     * @return 返回修改数据的行数
     */
    Integer updateOne(LifeCycleTactics t);

    // 批量修改数据
}
