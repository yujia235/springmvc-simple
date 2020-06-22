package com.yujia.annotation;

import com.yujia.annotation.enums.RequestMethod;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequestMapping {

    String name() default "";

    String value() default "";

    RequestMethod[] method() default RequestMethod.GET;
}
