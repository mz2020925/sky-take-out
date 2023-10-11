package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
// TODO 为什么全局异常处理器没有添加@Component注解
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获SQL异常 - 账号(username)存入数据库重复了
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        System.out.println(ex.getMessage());
        System.out.println(ex.toString());
        if(message.contains("Duplicate entry")){
            // String[] strings = message.split(" ");
            // String duplicateUsername = strings[2];
            String msg = message;
            if (message.contains("idx_category_name")){
                msg = MessageConstant.CATEGORY_NAME_DUPLICATE;

            }else if(message.contains("idx_username")){
                msg = MessageConstant.ACCOUNT_ALREADY_EXISTS;
            }
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

    }
}
