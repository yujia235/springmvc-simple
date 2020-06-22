package com.yujia.annotation;

import com.yujia.annotation.enums.Propagation;

import java.lang.annotation.*;

/**
 * 自定义@Transactional注解
 *      生命周期：运行时保留
 *      目标对象：类|接口，方法
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD})
public @interface Transactional {
    Propagation propagation() default Propagation.REQUIRED;
    boolean readOnly() default false;
}
