package com.tzp.LifeCycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 启动类
 * EnableAspectJAutoProxy 启用切面功能
 *
 * @author kangxudong
 */
@SpringBootApplication
@MapperScan("com.tzp.LifeCycle.mapper")
@EnableAspectJAutoProxy
public class LifeCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeCycleApplication.class, args);
        System.out.println("http://localhost:9990/doc.html");
    }

}
