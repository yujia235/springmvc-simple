package com.yujia.factory;

import com.yujia.annotation.*;
import com.yujia.utils.PackageScanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class BeanFactory {

    /**
     * 原生bean<beanName, bean>
     */
    private static final Map<String, Object> singletonBeanMap = new HashMap();
    /**
     * 原生bean<className|superClassName, bean>
     */
    private static final Map<String, Object> classNameBeanMap = new HashMap();
    /**
     * 代理bean<beanName, bean>
     */
    private static final Map<String, Object> proxySingletonBeanMap = new HashMap();
    /**
     * 代理bean<className|superClassName, bean>
     */
    private static final Map<String, Object> proxyClassNameBeanMap = new HashMap();

    static {
        // 创建单例池
        createBeanMap();
        // 组装beanMap
        handleClassNameBeanMap();
        // 注入bean
        diBean();
        // 代理
        handleProxy();
    }

    /**
     * 创建单例池
     */
    private static void createBeanMap() {
        // 扫描包路径下所有的class文件
        Set<Class<?>> classSet = PackageScanUtils.getClzFromPkg("com.yujia");
        // 过滤掉接口和注解
        classSet = classSet.stream().filter(c -> !c.isAnnotation() && !c.isInterface() && !c.getName().startsWith("com.yujia.servert"))
                .collect(Collectors.toSet());
        // 创建bean
        for (Class<?> c : classSet) {
            if (c.isAnnotationPresent(Component.class)) {
                String value = c.getAnnotation(Component.class).value();
                try {
                    singletonBeanMap.put(StringUtils.isEmpty(value) ? c.getSimpleName() : value, c.getDeclaredConstructor().newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (c.isAnnotationPresent(Service.class)) {
                String value = c.getAnnotation(com.yujia.annotation.Service.class).value();
                try {
                    singletonBeanMap.put(StringUtils.isEmpty(value) ? c.getSimpleName() : value, c.getDeclaredConstructor().newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }

    /**
     * 组装beanMap
     * key：自身className|父类className|父接口className
     * value：bean对象
     */
    private static void handleClassNameBeanMap() {
        singletonBeanMap.values().forEach(bean -> {
            getSuperClassNameSet(bean).forEach(className -> classNameBeanMap.put(className, bean));
        });
    }

    private static Set<String> getSuperClassNameSet(Object bean) {
        Set<String> classNameSet = new HashSet<>();
        Class<?> cls = bean.getClass();
        // 自身
        classNameSet.add(cls.getName());
        // 父类
        Class<?> superclass = cls.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            classNameSet.add(superclass.getName());
        }
        // 父接口
        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            Arrays.stream(interfaces).forEach(in -> classNameSet.add(in.getName()));
        }
        return classNameSet;
    }

    /**
     * 属性注入
     */
    private static void diBean() {
        for (Map.Entry<String, Object> entry : singletonBeanMap.entrySet()) {
            Object bean = entry.getValue();
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            List<Field> fieldList = Arrays.stream(declaredFields).filter(field -> field.isAnnotationPresent(com.yujia.annotation.Autowired.class)).collect(Collectors.toList());
            for (Field field : fieldList) {
                Object value = classNameBeanMap.get(field.getType().getName());
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(bean, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 代理（事务控制）
     */
    private static void handleProxy() {
        String key;
        Object value;
        com.yujia.factory.ProxyFactory proxyFactory = getBeanByType(com.yujia.factory.ProxyFactory.class);
        for (Map.Entry<String, Object> entry : singletonBeanMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            Object proxy = proxyFactory.proxy(value, Boolean.TRUE);
            proxySingletonBeanMap.put(key, proxy);
            getSuperClassNameSet(value).forEach(className -> proxyClassNameBeanMap.put(className, proxy));

        }
    }

    public static <T> T getBeanByName(String beanName) {
        return (T) singletonBeanMap.get(beanName);
    }

    public static <T> T getBeanByType(Class<T> clazz) {
        return (T) classNameBeanMap.get(clazz.getName());
    }

    public static <T> T getProxyBeanByName(String beanName) {
        return (T) proxySingletonBeanMap.get(beanName);
    }

    public static <T> T getProxyBeanByType(Class<T> clazz) {
        return (T) proxyClassNameBeanMap.get(clazz.getName());
    }
}
