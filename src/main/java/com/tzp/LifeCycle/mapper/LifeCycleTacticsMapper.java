package com.tzp.LifeCycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;


/**
 * LifeCycleChange访问数据库的mapper
 *
 * @author kangxvdong
 */
@Mapper
public interface LifeCycleTacticsMapper extends BaseMapper<LifeCycleTactics> {

    /**
     * 根据表名，主键和主键值查询对象
     *
     * @param tableName 表名
     * @param primaryKeyName 主键
     * @param primaryKeyValue 主键值
     * @return 返回一个对象
     */
    @Select("SELECT * FROM ${tableName} WHERE ${primaryKeyName} = #{primaryKeyValue}")
    Map<String, Object> selectByPrimaryKey(String tableName, String primaryKeyName, String primaryKeyValue);

    /**
     * 根据表名，主键，主键值，删除一行数据
     *
     * @param tableName 表名
     * @param primaryKeyName 主键
     * @param primaryKeyValue 主键值
     * @return 一个对象
     */
    @Delete("DELETE FROM ${tableName} WHERE ${primaryKeyName} = #{primaryKeyValue}")
    Integer deleteByPrimaryKey(String tableName, String primaryKeyName, String primaryKeyValue);

}
