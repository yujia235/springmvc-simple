package com.yujia.factory.utils;

import com.yujia.annotation.Component;
import com.yujia.annotation.Controller;
import com.yujia.annotation.Service;
import com.yujia.utils.PackageScanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanUtils {

    /**
     * 加载资源
     *
     * @param inStream
     * @throws IOException
     */
    public static Properties loadResource(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inStream);
        return properties;
    }

    /**
     * 包扫描
     *
     * @return
     */
    public static Set<Class<?>> packageScan(String path) {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException(path + " is blank");
        }
        // 扫描包路径下所有的class文件，过滤掉注解&&接口&&非标注注解类
        return PackageScanUtils.getClzFromPkg(path).stream().filter
                (c -> !c.isAnnotation() && !c.isInterface() &&
                        (c.isAnnotationPresent(Component.class) || c.isAnnotationPresent(Service.class) || c.isAnnotationPresent(Controller.class)))
                .collect(Collectors.toSet());
    }

    /**
     * 获取指定的beanName
     *
     * @param c
     * @return
     */
    public static String createBeanName(Class<?> c) {
        String beanName = null;
        if (c.isAnnotationPresent(Component.class)) {
            beanName = c.getAnnotation(Component.class).value();
            beanName = StringUtils.isEmpty(beanName) ? BeanUtils.lowerFirst(c.getSimpleName()) : beanName;
        }
        if (c.isAnnotationPresent(Service.class)) {
            beanName = c.getAnnotation(Service.class).value();
            beanName = StringUtils.isEmpty(beanName) ? BeanUtils.lowerFirst(c.getSimpleName()) : beanName;
        }
        if (c.isAnnotationPresent(Controller.class)) {
            beanName = c.getAnnotation(Controller.class).value();
            beanName = BeanUtils.lowerFirst(c.getSimpleName());
        }
        return beanName;
    }

    public static Set<Class<?>> getSuperClassNameSet(Class<?> cls) {
        Set<Class<?>> classNameSet = new HashSet<>();
        // 自身
        classNameSet.add(cls);
        // 父类
        Class<?> superclass = cls.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            classNameSet.add(superclass);
        }
        // 父接口
        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            Arrays.stream(interfaces).forEach(in -> classNameSet.add(in));
        }
        return classNameSet;
    }

    /**
     * ⾸字⺟⼩写
     */

    public static String lowerFirst(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        if ('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }
}
