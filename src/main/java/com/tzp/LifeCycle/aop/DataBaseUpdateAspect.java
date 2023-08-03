package com.tzp.LifeCycle.aop;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.tzp.LifeCycle.dto.DataBaseUpdateDto;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 执行数据库行数据更新完毕之后，策略的更新方法
 *
 * @author kangxvdong
 */
@Slf4j
@Aspect
@Component
public class DataBaseUpdateAspect<T> {

    @Autowired
    private LifeCycleTacticsService lifeCycleTacticsService;

    @Pointcut("@annotation(com.tzp.LifeCycle.aop.annotation.DataBaseUpdate)")
    public void dataBaseUpdatePointcut() {
    }

    @After("dataBaseUpdatePointcut()")
    public void afterDataBaseUpdate(JoinPoint joinPoint) {
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
        // 获取此次更新中，表主键列的值
        String keyValue = SqlHelper.table(clazz).getPropertyValue(lifeUpdateTactics.getTObject(), keyProperty).toString();
        String schedulerId = tableName + "+" + keyProperty + "+" + keyValue;
        LifeCycleTactics lifeCycleTactics = lifeCycleTacticsService.queryBySchedulerId(schedulerId);
        // 给策略添加一些必要的值
        LifeCycleTactics tactics = lifeUpdateTactics.getLifeCycleTactics();
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
    }

}
