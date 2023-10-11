package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SQL语句公共字段自动填充
 */
@Target(ElementType.METHOD)  // 表示自定义的这个注解修饰的是方法
@Retention(RetentionPolicy.RUNTIME)  // @Retention注解用于指明修饰的注解的生存周期，即会保留到哪个阶段。就是这个自定义注解的生命周期，为什么这个注解在RUNTIME结束后就销毁呢？
/*
1、RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
2、RetentionPolicy.CLASS：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
3、RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；

这3个生命周期分别对应于：Java源文件(.java文件) ---> .class文件 ---> 内存中的字节码。
 */
public @interface AutoFill {
    /**
     * 数据库操作类型
     * @return
     * */
    OperationType value();  // 填充哪种SQL语句呢？对create_time、create_user、update_time、update_user进行insert才做。对update_time、update_user进行update操作
}