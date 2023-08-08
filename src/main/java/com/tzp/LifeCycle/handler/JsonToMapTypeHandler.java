package com.tzp.LifeCycle.handler;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * MyBatis-Plus将数据库中的JsonString（text、varchar2等）转换为Map<String, String>
 * 也可以将Java中的Map<String, String>在存进去的时候转JsonString（text、varchar2）
 *
 * @author kangxvdong
 */
public class JsonToMapTypeHandler extends BaseTypeHandler<Map<String, String>> {

    private Class<Map<String, String>> mapClass;

    /**
     * 在执行数据库插入操作时，将 Java 对象的值设置到 PreparedStatement 对象中。
     * 方法将 Java 对象中的 Map 转换为 JSON 字符串，并将其设置到 PreparedStatement 对象中，以便在数据库插入操作时使用。
     *
     * @param preparedStatement 预编译的 SQL 语句
     * @param i 是参数的位置
     * @param stringStringMap 是 Java 对象的值
     * @param jdbcType 是 JDBC 类型。
     * @throws SQLException 异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<String, String> stringStringMap, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, JSON.toJSONString(stringStringMap));
    }

    /**
     * 在从数据库结果集中获取数据时，将数据库字段的值转换为 Java 对象。
     * 方法从数据库结果集中获取 JSON 字符串，然后将其解析为 Map 对象，以便在查询操作时使用。
     *
     * @param resultSet 结果集
     * @param s 字段名。
     * @return 结果
     * @throws SQLException 异常
     */
    @Override
    public Map<String, String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String jsonStr = resultSet.getString(s);
        return JSON.parseObject(jsonStr, mapClass);
    }

    /**
     * 与第二个方法类似，不过这里通过字段索引获取字段值。
     *
     * @param resultSet 结果集
     * @param i 索引位置
     * @return 结果
     * @throws SQLException 异常
     */
    @Override
    public Map<String, String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String jsonStr = resultSet.getString(i);
        return JSON.parseObject(jsonStr, mapClass);
    }

    /**
     * 与第三个方法类似，不过这里用于处理存储过程的结果。
     *
     * @param callableStatement 类似结果集
     * @param i 索引位置
     * @return 结果
     * @throws SQLException 异常
     */
    @Override
    public Map<String, String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonStr = callableStatement.getString(i);
        return JSON.parseObject(jsonStr, mapClass);
    }

}
