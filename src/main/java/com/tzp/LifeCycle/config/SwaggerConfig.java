package com.tzp.LifeCycle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;

/**
 * swagger配置
 *
 * @author kangxvdong
 * @date 2023/07/14 18:25
 */
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        // 添加接口请求头参数配置 没有的话 可以忽略
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                // 关闭默认状态码
                .useDefaultResponseMessages(false)
                // 是否启动swagger 默认启动
                .enable(true)
                // 所在分组
                .groupName("数据生命周期")
                .select()
                // 指定扫描的包路径
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo("数据生命周期工具",
                "数据生命周期工具接口部分",
                "v1.0",
                "http://localhost/",
                //作者信息
                new Contact("康旭东", "", ""),
                "Apache 2.0",
                "",
                new ArrayList<>());
    }

}
