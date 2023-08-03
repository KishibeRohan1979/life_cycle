package com.tzp.LifeCycle.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对数据库一些删除请求的特殊操作注解
 * 主要作用是，当数据库中的数据被删除时，同步将策略，定时任务全部删除
 *
 * @author kangxvdong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBaseDelete {
}
