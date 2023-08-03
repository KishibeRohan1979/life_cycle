package com.tzp.LifeCycle.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.config.QuartzConfig;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.mapper.LifeCycleTacticsMapper;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author kangxvdong
 */

@Slf4j
@Service
public class LifeCycleTacticsServiceImpl implements LifeCycleTacticsService {

    @Autowired
    private LifeCycleTacticsMapper lifeCycleTacticsMapper;

    @Autowired
    private QuartzConfig scheduler;

    /**
     * 根据id查询数据
     *
     * @param id 要查询对象的id
     * @return 返回查询的数据
     */
    @Override
    public LifeCycleTactics queryById(Serializable id) {
        return lifeCycleTacticsMapper.selectById(id);
    }

    /**
     * 根据定时id查询数据
     *
     * @param schedulerId 要查询对象的定时id
     * @return 返回查询的数据
     */
    @Override
    public LifeCycleTactics queryBySchedulerId(String schedulerId) {
        QueryWrapper<LifeCycleTactics> wrapper = new QueryWrapper<>(null);
        wrapper.eq("scheduler_id", schedulerId);
        return lifeCycleTacticsMapper.selectOne(wrapper);
    }

    /**
     * 根据字段条件模糊查询返回分页查询结果
     *
     * @param dto 要查询的查询对象类
     * @return 返回查询的数据集合
     */
    @Override
    public Page<LifeCycleTactics> queryList(DataBaseQueryDto dto) {
        Page<LifeCycleTactics> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<LifeCycleTactics> wrapper = new QueryWrapper<>(null);
        // 拼接显示字段的方法
        wrapper.select("distinct *");
        if (dto.getQueryAllEqualFields() != null && !dto.getQueryAllEqualFields().isEmpty()) {
            wrapper.allEq(dto.getQueryAllEqualFields());
        }
        return lifeCycleTacticsMapper.selectPage(page, wrapper);
    }

    /**
     * 添加一条数据
     *
     * @param t 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    @Override
    public Integer createOne(LifeCycleTactics t) {
        // 先向数据库添加数据
        int successNum = lifeCycleTacticsMapper.insert(t);
        try {
            // 添加完成之后创建一个定时任务；根据提供的执行时间时间戳，执行一次
            scheduler.createJobAtTimestamp(t.getSchedulerId(), t.getExecutionTime());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return successNum;
    }

    /**
     * 批量添加数据
     *
     * @param list 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    @Override
    public Integer createByList(List<LifeCycleTactics> list) {
        return null;
    }

    /**
     * 根据对象删除一条数据；
     *
     * @param t 删除依据的对象
     * @return 返回删除数据的行数
     */
    @Override
    public Integer deleteOne(LifeCycleTactics t) {
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(t), Map.class);
        // 因为数据库字段和map对应不上，用这个算法替换一下key
        map = LifeStringUtil.convertMapKeys(map);
        // 利用转换好的map删除对应数据
        int successNum = lifeCycleTacticsMapper.deleteByMap(map);
        try {
            scheduler.deleteJob(map.get("scheduler_id").toString());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return successNum;
    }


    /**
     * 根据id修改一条数据
     *
     * @param t 准备好修改数据的对象
     * @return 返回修改数据的行数
     */
    @Override
    public Integer updateOne(LifeCycleTactics t) {
        int successNum = lifeCycleTacticsMapper.updateById(t);
        try {
            // 销毁原先的定时任务，创建新的定时任务
            scheduler.deleteJob(t.getSchedulerId());
            scheduler.createJobAtTimestamp(t.getSchedulerId(), t.getExecutionTime());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return successNum;
    }

}
