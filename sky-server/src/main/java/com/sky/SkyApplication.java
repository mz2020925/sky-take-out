package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement // 开启注解方式的事务管理,
/*
本项目的菜品管理中的 批量删除接口 对应的 DishServiceImpl中删除方法本来应该有@Transactional修饰，表示该方法是事务模式。
为什么要给这个方法设置事务模式，因为这个方法中涉及多个表的操作，所以要改全部都改，要不改全都不改，不能因为中间出错导致有的表改了、有的表没改。
一个ServiceImpl方法，只要设计多表操作，必须用事务模式。只对一个表操作不用事务模式。
*/
@Slf4j
@EnableCaching
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("sky-server 启动了!!!");
        // 后端开发指导：https://yapi.pro/group/121400
        // 前端联调测试接口 http://localhost:80/  倍被nginx转接到 http://localhost:8080/admin/
        // 后端接口自己测试访问Swagger http://localhost:8080/doc.html
    }
}
