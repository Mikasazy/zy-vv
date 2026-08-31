package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自定义切面，用于自动填充字段
@Aspect//切面类，用于拦截mapper中的所有有AutoFill注解的方法
@Component//组件类，用于自动扫描
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    //切入点，用于拦截mapper中的所有有AutoFill注解的方法
    public void autoFillPointCut() {
    }

    //前置通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始自动填充字段");
        //获取被拦截方法上的注解中的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取注解对象
        OperationType operationType = autoFill.value();//获取注解中的数据库操作类型
        //获取到当前拦截方法的参数对象
        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || args[0] == null) {
            return;
        }

        Object entity = args[0];//获取第一个参数对象，即员工对象
        //准备赋值的字段
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();//获取当前登录员工的id
        //根据不同数据库操作类型，自动填充字段
        if (operationType == OperationType.INSERT) {
            //为四个字段赋值当前时间、当前登录员工的id
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);//设置创建时间为当前时间
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);//设置创建人当前登录员工的id
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);//设置更新人当前登录员工的id
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);//设置更新时间为当前时间
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);//设置创建时间为当前时间
                setCreateUser.invoke(entity, currentId);//设置创建人当前登录员工的id
                setUpdateUser.invoke(entity, currentId);//设置更新人当前登录员工的id
                setUpdateTime.invoke(entity, now);//设置更新时间为当前时间
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //为两个字段赋值当前时间、当前登录员工的id
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);//设置更新人当前登录员工的id
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);//设置更新时间为当前时间
                //通过反射为对象属性赋值
                setUpdateUser.invoke(entity, currentId);//设置更新人当前登录员工的id
                setUpdateTime.invoke(entity, now);//设置更新时间为当前时间
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}