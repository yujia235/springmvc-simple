package com.yujia.annotation;

import java.lang.annotation.*;

/**
 * 自定义@Controller注解
 * 生命周期：运行时保留
 * 目标对象：类|接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    String value() default "";
}
