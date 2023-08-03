package com.tzp.LifeCycle.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对数据库一些更新请求的特殊操作注解
 * 当数据库的数据更新时，策略也随之更新，或创建、或更新、或删除。定时任务也随之发生改变
 *
 *
 * @author kangxvdong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBaseUpdate {
}
