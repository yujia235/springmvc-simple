package com.yujia.annotation;

import java.lang.annotation.*;

/**
 * 自定义@Component注解
 *      生命周期：运行时保留
 *      目标对象：属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
    String value() default "";
}
