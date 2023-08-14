package com.tzp.LifeCycle.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tzp.LifeCycle.aop.annotation.EsAccess;
import com.tzp.LifeCycle.dto.EsDto;
import com.tzp.LifeCycle.entity.LifeCycleDataTable;
import com.tzp.LifeCycle.entity.LifeCycleTactics;
import com.tzp.LifeCycle.enums.DataAccessType;
import com.tzp.LifeCycle.service.LifeCycleDataTableService;
import com.tzp.LifeCycle.service.LifeCycleTacticsService;
import com.tzp.LifeCycle.util.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 执行Es行数据操作完毕之后，策略的更新方法
 *
 * @author kangxvdong
 */
@Slf4j
@Aspect
@Component
public class EsAccessAspect {

    @Autowired
    private LifeCycleTacticsService lifeCycleTacticsService;

    @Autowired
    private LifeCycleDataTableService lifeCycleDataTableService;

    @Pointcut("@annotation(com.tzp.LifeCycle.aop.annotation.EsAccess)")
    public void esAccessPointcut() {
    }

    @Around("esAccessPointcut()")
    public Object aroundEsAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法的返回值
        Object returnValue = joinPoint.proceed();
        MsgUtil msgUtil = JSON.parseObject(JSON.toJSONString(returnValue), MsgUtil.class);
        if (!msgUtil.getFlag()) {
            // 如果执行失败了，@After就不执行了
            return msgUtil;
        }
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        EsDto lifeTactics = (EsDto) args[0];

        // 策略类用来直接添加、更新、删除策略
        String indexName = lifeTactics.getIndexName();
        // 第一步,查询对应设计表存储的主键内容
        LifeCycleDataTable table = lifeCycleDataTableService.queryByIndex(indexName);
        if (table == null) {
            return MsgUtil.fail();
        }
        String keyProperty = table.getPrimaryKeyName();
        List<Map<String, Object>> mapList = lifeTactics.getDataList();
        List<String> schedulerIds = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            schedulerIds.add(indexName + "+" + keyProperty + "+" + map.get(keyProperty));
        }

        // 获取方法上的注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        EsAccess esAccessAnnotation = method.getAnnotation(EsAccess.class);
        DataAccessType accessType = esAccessAnnotation.accessType();

        LifeCycleTactics tactics = lifeTactics.getLifeCycleTactics();
        switch (accessType) {
            case UPDATE:
                long nowTime = System.currentTimeMillis();
                tactics.setLastAccessTime(nowTime);
                tactics.setExecutionTime(nowTime + tactics.getDeadline());
                tactics.setAccessAddress(indexName);
                // 往里添加的时候，截掉前面的“class ”这几个字符，有个空格
                tactics.setDataObjectType(JSONObject.class.toString().substring(6));
                Map<String, Object> tableFields = JSON.parseObject(table.getAllFields().toString());
                tactics.setPrimaryKeyName(keyProperty);
                tactics.setPrimaryKeyType(tableFields.get(keyProperty).toString());
                // 数据库查询结果一定小于等于 mapList 的长度（因为 scheduler_id 在数据库是唯一的）
                List<LifeCycleTactics> lifeCycleTacticsList = lifeCycleTacticsService.queryBySchedulerIds(schedulerIds);
                for (Map<String, Object> map : mapList) {
                    String schedulerId = indexName + "+" + keyProperty + "+" + map.get(keyProperty);
                    tactics.setSchedulerId(schedulerId);
                    tactics.setDetailedData(JSON.toJSONString(map));
                    tactics.setPrimaryKeyValue(map.get(keyProperty).toString());
                    // 判断当前的 tactics（通过schedulerId） 是否存在于 lifeCycleTacticsList
                    LifeCycleTactics lifeCycleTactics = findLifeCycleTactics(lifeCycleTacticsList, schedulerId);
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
                break;
            case DELETE:
                // 一个删除的操作
                // 删除策略表中，对应行的策略值
                lifeCycleTacticsService.deleteListBySchedulerId(schedulerIds);
                break;
            default:
                break;
        }
        return msgUtil;
    }

    /**
     * 判断当前的 tactics（通过schedulerId） 是否存在于 lifeCycleTacticsList
     * 由于对比列表的意义只是用来对比，所以在循环找到的时候，会删除找到的元素，来缩减 list 大小，整体优化循环的次数
     *
     * @param lifeCycleTacticsList 对比列表
     * @param schedulerId 对比使用的id
     * @return 对象，如果找不到就是null
     */
    private LifeCycleTactics findLifeCycleTactics(List<LifeCycleTactics> lifeCycleTacticsList, String schedulerId) {
        LifeCycleTactics foundTactics = null;
        Iterator<LifeCycleTactics> iterator = lifeCycleTacticsList.iterator();

        while (iterator.hasNext()) {
            LifeCycleTactics tactics = iterator.next();
            if (tactics.getSchedulerId().equals(schedulerId)) {
                foundTactics = tactics;
                // 使用迭代器从列表中删除对象
                iterator.remove();
                // 一旦找到就中断循环
                break;
            }
        }

        return foundTactics;
    }

}
