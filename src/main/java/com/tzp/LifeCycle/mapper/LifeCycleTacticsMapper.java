package com.tzp.LifeCycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import org.apache.ibatis.annotations.Mapper;


/**
 * LifeCycleChange访问数据库的mapper
 *
 * @author kangxudong
 */
@Mapper
public interface LifeCycleTacticsMapper extends BaseMapper<LifeCycleTactics> {
}
