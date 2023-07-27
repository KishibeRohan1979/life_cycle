package com.tzp.LifeCycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tzp.LifeCycle.entity.LifeTest;
import org.apache.ibatis.annotations.Mapper;

/**
 * LifeTest访问数据库的mapper
 *
 * @author kangxudong
 */

@Mapper
public interface LifeTestMapper extends BaseMapper<LifeTest> {

}
