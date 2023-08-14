package com.tzp.LifeCycle.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.config.QuartzConfig;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.mapper.LifeCycleDataTableMapper;
import com.tzp.LifeCycle.mapper.LifeCycleTacticsMapper;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import com.tzp.LifeCycle.util.PageUtil;
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
     * 根据所有定时id查询数据
     *
     * @param schedulerIds 要查询对象的所有定时id
     * @return 返回所有查询的数据
     */
    @Override
    public List<LifeCycleTactics> queryBySchedulerIds(List<String> schedulerIds) {
        QueryWrapper<LifeCycleTactics> wrapper = new QueryWrapper<>(null);
        wrapper.in("scheduler_id", schedulerIds);
        return lifeCycleTacticsMapper.selectList(wrapper);
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
     * 至于说为什么这个方法这么写呢，是因为比如数据库里的键叫index_name这里直接用对象，就发送的是indexName，导致找不到字段
     * 所以就用了这么个很奇怪的方法
     *
     * @param t 删除依据的对象
     * @return 返回删除数据的行数
     */
    @Override
    public Integer deleteOne(LifeCycleTactics t) {
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(t));
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
     * 根据 schedulerId 列表批量删除数据；
     *
     * @param schedulerIdList 删除依据的列表
     * @return 返回删除数据的行数
     */
    @Override
    public Integer deleteListBySchedulerId(List<String> schedulerIdList) {
        QueryWrapper<LifeCycleTactics> wrapper = new QueryWrapper<>(null);
        wrapper.in("scheduler_id", schedulerIdList);
        int successNum = lifeCycleTacticsMapper.delete(wrapper);
        try {
            for (String schedulerId : schedulerIdList) {
                scheduler.deleteJob(schedulerId);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return successNum;
    }

    /**
     * 当删除索引、文件夹、数据表时，删除对应索引下的所有数据、定时任务
     * 删除where access_address = value的所有行数
     *
     * @param accessAddress 删除accessAddress依据的值
     * @return 返回删除数据的行数
     */
    @Override
    public Integer deleteByAccessAddressValue(String accessAddress) {
        QueryWrapper<LifeCycleTactics> wrapper = new QueryWrapper<>(null);
        wrapper.eq("access_address", accessAddress);

        // 删除springboot中的定时任务
        // 至于为什么这里没有使用安全方法，是因为定时策略里没有包含任何有效的敏感或者有效信息
        // 就算删除失败了，也最多是定时任务执行失败，不影响整个项目效率
        List<LifeCycleTactics> tacticsList = PageUtil.getPageListByDataBase(lifeCycleTacticsMapper, wrapper);
        for (LifeCycleTactics tactic : tacticsList) {
            try {
                scheduler.deleteJob(tactic.getSchedulerId());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

        return lifeCycleTacticsMapper.delete(wrapper);
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
