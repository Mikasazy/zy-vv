package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

//自定义注解，用于自动填充字段
@Target(ElementType.METHOD)
public @interface AutoFill {
    //数据库操作类型: 插入insert/更新update
    OperationType value();
}
