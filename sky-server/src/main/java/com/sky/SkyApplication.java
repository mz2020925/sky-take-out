package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("sky-server 启动了!!!");
        // 后端开发指导：https://yapi.pro/group/121400
        // 前后端联调测试接口 http://localhost:80
        // 后端接口自己测试访问Swagger http://localhost:8080/doc.html
    }
}
