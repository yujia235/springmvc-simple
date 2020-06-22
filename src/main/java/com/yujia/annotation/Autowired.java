package com.yujia.annotation;

import java.lang.annotation.*;

/**
 * 自定义@Autowired注解
 *      生命周期：运行时保留
 *      目标对象：属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Autowired {
    boolean required() default true;
}
