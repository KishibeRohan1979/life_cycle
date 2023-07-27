package com.tzp.LifeCycle.aop;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tzp.LifeCycle.entity.JdbcRequestInfo;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.enums.DataAccessType;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;


/**
 * 利用ibatis框架实现拦截jdbc请求
 *
 * @author kangxuodong
 */
@Slf4j
@Aspect
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataBaseRequestAspect implements Interceptor {

    @Autowired
    private LifeCycleTacticsService lifeCycleChangeService;

    private static final ThreadLocal<JdbcRequestInfo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 在使用 @DataBaseRequest 注解的方法执行后执行，用于获取拦截器中保存的数据库请求信息。
     */
    @After("@annotation(com.tzp.LifeCycle.aop.annotation.DataBaseRequest)")
    private void someOtherMethod() {
        JdbcRequestInfo jdbcRequestInfo = THREAD_LOCAL.get();
        if (jdbcRequestInfo != null) {
            // 请求的表名
            String tableName = jdbcRequestInfo.getTableName();
            // 请求的数据
            Object requestObject = jdbcRequestInfo.getRequestObject();
            // 对象的原类型；.getSimpleName()简写；toString()详细
            String objectClassType = requestObject.getClass().getSimpleName();
            // 转json字符串
            String requestObjectStr = JSON.toJSONString(requestObject);
            // 发送请求的类型
            DataAccessType jdbcType = jdbcRequestInfo.getJdbcType();
            // 请求表的主键
            String primaryKey = "";
            // 请求表主键的类型
            String primaryKeyType = "";
            // 请求表主键的值
            String primaryKeyValue = "";

            // 获取表的主键信息
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
            if (tableInfo != null) {
                primaryKey = tableInfo.getKeyProperty();
                if (StringUtils.isNotEmpty(primaryKey)) {
                    // 利用Java反射获取请求数据的属性
                    Class<?> requestClass = requestObject.getClass();
                    try {
                        Field primaryKeyField = requestClass.getDeclaredField(primaryKey);
                        Class<?> attributeType = primaryKeyField.getType();
                        primaryKeyType = attributeType.getSimpleName();
                        // 获取get...（get属性）方法
                        Method getNameMethod = requestClass.getMethod("get" + LifeStringUtil.capitalizeFirstLetter(primaryKey));
                        // 调用get...（get属性）方法
                        Object fieldValue = getNameMethod.invoke(requestObject);
                        primaryKeyValue = fieldValue.toString();
                    } catch (Exception e) {
                        log.error("对应类没有设置 " + primaryKey + " 字段");
                        e.printStackTrace();
                    }
                } else {
                    log.error("表 " + tableName + " 没有设置主键。");
                }
            } else {
                log.error("找不到表 " + tableName + " 的元数据信息。");
            }

            long nowTime = System.currentTimeMillis();

            // 定时任务id
            String schedulerId = tableName + "+" + primaryKey + "+" + primaryKeyValue;

            if (jdbcType == DataAccessType.DELETE) {
                // 一个删除的操作
                LifeCycleTactics deleteLiCyTa = new LifeCycleTactics(
                        // 定时任务id，该字段是唯一的（约定使用（表名+主键名+主键值）当作定时任务id）
                        schedulerId,
                        // 访问地址（即关系型数据库表名、ES索引、文件路径等）
                        tableName,
                        // 主键（关系型数据库表的主键、es索引的_id、文件的具体文件名）
                        LifeStringUtil.convertString(primaryKey),
                        // 主键的值
                        primaryKeyValue
                );
                lifeCycleChangeService.deleteOne(deleteLiCyTa);
                log.info("删除对应数据");
            } else {
                log.info("其他操作不做处理");
            }

        }
        // 清理数据
        THREAD_LOCAL.remove();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        Statement statement = CCJSqlParserUtil.parse(statementHandler.getBoundSql().getSql());
        // 获取sql
        String sql = statement.toString();
        // 根据sql获取表名
        String tableName = getTableNameFromSql(statement);
        // 获取请求的数据
        Object requestObject = statementHandler.getParameterHandler().getParameterObject();
        // 获取请求类型
        DataAccessType requestType = getRequestTypeFromSql(statement);

        JdbcRequestInfo jdbcRequestInfo = new JdbcRequestInfo(sql, tableName, requestObject, requestType);

        // 传输数据
        THREAD_LOCAL.set(jdbcRequestInfo);

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 配置拦截器代码
    }

    /**
     * 获取请求的类型
     *
     * @param statement 截取sql的类
     * @return DataChangeType枚举类里的值
     */
    private DataAccessType getRequestTypeFromSql(Statement statement) {
        if (statement instanceof Select) {
            return DataAccessType.SELECT;
        } else if (statement instanceof Insert) {
            return DataAccessType.INSERT;
        } else if (statement instanceof Update) {
            return DataAccessType.UPDATE;
        } else if (statement instanceof Delete) {
            return DataAccessType.DELETE;
        }
        return null;
    }

    /**
     * 返回增删改请求的表名，查询太复杂先不搞了
     *
     * @param statement 截取sql的类
     * @return 数据表的表名
     */
    private String getTableNameFromSql(Statement statement) {
        if (statement instanceof Select) {
            // 查询方法直接返回sql，因为查询比较复杂，from后面可能是另外一个查询，所以直接返回sql
            return ((Select) statement).getSelectBody().toString();
        } else if (statement instanceof Insert) {
            return ((Insert) statement).getTable().getName();
        } else if (statement instanceof Update) {
            return ((Update) statement).getTable().getName();
        } else if (statement instanceof Delete) {
            return ((Delete) statement).getTable().getName();
        }
        return null;
    }

}
