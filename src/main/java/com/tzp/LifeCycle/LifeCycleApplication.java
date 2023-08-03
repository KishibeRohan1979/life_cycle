package com.tzp.LifeCycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动类
 * EnableAspectJAutoProxy 启用切面功能
 * EnableAsync 启用线程池
 *
 * @author kangxvdong
 */
@SpringBootApplication
@MapperScan("com.tzp.LifeCycle.mapper")
@EnableAspectJAutoProxy
@EnableAsync
public class LifeCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeCycleApplication.class, args);
        System.out.println("http://localhost:9990/doc.html");
    }

}
