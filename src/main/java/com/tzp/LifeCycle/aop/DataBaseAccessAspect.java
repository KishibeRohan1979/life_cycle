package com.tzp.LifeCycle.aop;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.tzp.LifeCycle.aop.annotation.DataBaseAccess;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.enums.DataAccessType;
import com.tzp.LifeCycle.service.EsIndexService;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import com.tzp.LifeCycle.util.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 执行数据库行数据操作完毕之后，策略的更新方法
 *
 * @author kangxvdong
 */
@Slf4j
@Aspect
@Component
public class DataBaseAccessAspect<T> {

    @Autowired
    private LifeCycleTacticsService lifeCycleTacticsService;

    /**
     * 决定是否继续执行After的变量
     */
    private boolean notExecution = false;

    @Pointcut("@annotation(com.tzp.LifeCycle.aop.annotation.DataBaseAccess)")
    public void dataBaseAccessPointcut() {
    }

    @Around("dataBaseAccessPointcut()")
    public MsgUtil aroundDataBaseAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法的返回值
        Object returnValue = joinPoint.proceed();
        MsgUtil msgUtil = JSON.parseObject(JSON.toJSONString(returnValue), MsgUtil.class);
        // 如果执行失败了，@After就不执行了
        notExecution = !msgUtil.getFlag();
        return msgUtil;
    }

    @After("dataBaseAccessPointcut()")
    public void afterDataBaseUpdate(JoinPoint joinPoint) {
        if (notExecution) {
            return;
        }
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        DataBaseUpdateDto<T> lifeUpdateTactics = (DataBaseUpdateDto<T>) args[0];
        // 策略类用来直接添加、更新、删除策略
        // 第一步，查看对应策略是否存在
        // 这一步是利用DataBaseUpdateDto<T>来获取T对应类映射数据库种的表名（使用的是mybatis-plus插件中的方法）
        Class<?> clazz = DataBaseUpdateDto.getClassFromGenericType(lifeUpdateTactics);
        String tableName = SqlHelper.table(clazz).getTableName();
        // 获取表主键列在Java中的映射
        String keyProperty = SqlHelper.table(clazz).getKeyProperty();
        // 获取此次请求中，表主键列的值
        String keyValue = SqlHelper.table(clazz).getPropertyValue(lifeUpdateTactics.getTObject(), keyProperty).toString();
        String schedulerId = tableName + "+" + keyProperty + "+" + keyValue;

        // 获取方法上的注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        DataBaseAccess dataBaseAccessAnnotation = method.getAnnotation(DataBaseAccess.class);
        DataAccessType accessType = dataBaseAccessAnnotation.accessType();

        LifeCycleTactics tactics = lifeUpdateTactics.getLifeCycleTactics();
        switch (accessType) {
            case UPDATE:
                LifeCycleTactics lifeCycleTactics = lifeCycleTacticsService.queryBySchedulerId(schedulerId);
                // 给策略添加一些必要的值
                tactics.setSchedulerId(schedulerId);
                long nowTime = System.currentTimeMillis();
                tactics.setLastAccessTime(nowTime);
                tactics.setExecutionTime(nowTime + tactics.getDeadline());
                tactics.setAccessAddress(tableName);
                tactics.setDetailedData(JSON.toJSONString(lifeUpdateTactics.getTObject()));
                // 往里添加的时候，截掉前面的“class ”这几个字符，有个空格
                tactics.setDataObjectType(clazz.toString().substring(6));
                // 获取表主键列在数据库表的列名；String keyColumn = SqlHelper.table(LifeTest.class).getKeyColumn()
                tactics.setPrimaryKeyName(SqlHelper.table(clazz).getKeyColumn());
                tactics.setPrimaryKeyType(SqlHelper.table(clazz).getKeyType().toString().substring(6));
                tactics.setPrimaryKeyValue(keyValue);
                // 第二步，联立是否存在和周期是否为-1的情况（4种情况）分别做不同的操作
                if (lifeCycleTactics != null && tactics.getDeadline() == -1L) {
                    // 存在策略，但是修改的策略周期为-1，策略删除
                    lifeCycleTacticsService.deleteOne(lifeCycleTactics);
                } else if (lifeCycleTactics != null && tactics.getDeadline() != -1L) {
                    // 存在策略，但是修改的策略周期不为-1，策略更新
                    tactics.setTacticsId(lifeCycleTactics.getTacticsId());
                    if ( tactics.getTacticsType() == 2 ) {
                        tactics.setExecutionTime(lifeCycleTactics.getCreateTime() + tactics.getDeadline());
                    }
                    lifeCycleTacticsService.updateOne(tactics);
                } else if (lifeCycleTactics == null && tactics.getDeadline() != -1L) {
                    // 不存在策略，但是修改的策略周期不为-1，添加策略
                    tactics.setCreateTime(nowTime);
                    lifeCycleTacticsService.createOne(tactics);
                }
                // 不存在策略，但是修改的策略周期为-1，什么也不做
                break;
            case DELETE:
                // 一个删除的操作
                // 如果删除的是list（索引、文件夹）需要同步删除对应list（索引、文件夹）下含有的策略
                if ("list".equals(tactics.getDataType())) {
                    // 这个方法是删除该索引、文件夹、数据表下所有的策略数据、定时任务
                    lifeCycleTacticsService.deleteByAccessAddressValue(keyValue);
                    // 删除索引的方法，最好放在 Controller 里，这样的话，看的比较清楚，而且后续加删除文件可以方便一点
                }
                // 删除策略表中，对应表、行的策略值
                LifeCycleTactics deleteLiCyTa = new LifeCycleTactics(
                        // 定时任务id，该字段是唯一的（约定使用（表名+主键名+主键值）当作定时任务id）
                        schedulerId,
                        // 访问地址（即关系型数据库表名、ES索引、文件路径等）
                        tableName,
                        // 主键（关系型数据库表的主键、es索引的_id、文件的具体文件名）
                        LifeStringUtil.convertString(keyProperty),
                        // 主键的值
                        keyValue
                );
                lifeCycleTacticsService.deleteOne(deleteLiCyTa);
                break;
            default:
                break;
        }
    }

}
