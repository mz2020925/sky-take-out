package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


@Aspect  // 有了这个注解表示这个类，准确来说是这个Bean，就表示这个切面会滤出符合规则的方法，对这些方法执行advice操作
@Component  // 会在IOC容器中创建它的Bean
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    // TODO 这个AOP对DishMapper没有起作用
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}


    /**
     * 前置通知，在通知中进行公共字段的赋值 —— 看不懂？
     * 通俗来讲就是把 所有方法 经过规则autoFillPointCut()的过滤后，送到方法autoFill(JoinPoint joinPoint)中执行反射操作。
     * 对上面那句话进行概念解释：
     * 在SpringAOP中，所有方法 称为JoinPoint；
     * 规则autoFillPointCut()的过滤 称为Pointcut；对应注解是@Pointcut
     * 方法autoFill(JoinPoint joinPoint) 中的操作 称为advice；对应注解是@Before，@AfterReturning，@Around，@After，@AfterThrowing，参见：https://zhuanlan.zhihu.com/p/500555634
     * 上面三个合在一起 称为Aspect。对应注解是@Aspect
     */
    @Before("autoFillPointCut()")  //
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");
        // 下面很多反射操作
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();  // 获取方法签名对象，joinPoint应该是一个Mapper类里的一个方法，例如Employee类中的update()、insert()
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);  // 获得方法上的注解对象
        OperationType operationType = autoFill.value();  // 获得数据库操作类型,是insert还是update


        Object[] args = joinPoint.getArgs();  // 获取到当前被拦截方法的参数
        if (args == null ||args.length == 0){
            return;
        }

        // 如果上面没有return，则进行下面的操作
        Object entity = args[0];  // 被拦截方法的参数数组的第0元素entity，应该就是Employee对象、Category对象等等...

        // 准备待会赋值给变量的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据当前不同的操作类型，为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            // insert操作需要为为4个公共字段赋值
            try{
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            // update操作需要为2个公共字段操作
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }
}
