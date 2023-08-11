package com.tzp.LifeCycle.aop.annotation;

import com.tzp.LifeCycle.enums.DataAccessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对ES一些更新、删除、查询、添加请求的特殊操作注解
 * 当ES的数据更新、删除时，策略也随之更新，或创建、或更新、或删除。定时任务也随之发生改变
 *
 * @author kangxvdong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EsAccess {

    /**
     * 必填！！！获取请求类型
     *
     * @return 请求类型
     */
    DataAccessType accessType();

}
