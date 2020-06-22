package com.yujia.factory;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Controller;
import com.yujia.annotation.RequestMapping;
import com.yujia.factory.model.Handle;
import com.yujia.factory.utils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BeanFactory1 {

    private Properties properties;

    private Set<Class<?>> classSet;

    private Map<Class<?>, Set<Class<?>>> classMapping;

    /**
     * 原生bean<beanName, bean>
     */
    private static final Map<String, Object> singletonBeanMap = new HashMap();
    /**
     * 原生bean<className|superClassName, bean>
     */
    private static final Map<Class<?>, Object> classBeanMap = new HashMap();
    /**
     * 代理bean<beanName, bean>
     */
    private static final Map<String, Object> proxySingletonBeanMap = new HashMap();
    /**
     * 代理bean<className|superClassName, bean>
     */
    private static final Map<Class<?>, Object> proxyClassBeanMap = new HashMap();
    /**
     * 代理bean<className|superClassName, bean>
     */
    private static final Map<String, Handle> handleMapping = new HashMap();

    public BeanFactory1(InputStream inStream) throws Exception {
        initIoc(inStream);
    }

    private void initIoc(InputStream inStream) throws Exception {
        properties = BeanUtils.loadResource(inStream);
        classSet = BeanUtils.packageScan(properties.getProperty("com.yujia.package.scan"));
        classMapping();
        createBean();
        handlerMapping();
    }

    private void classMapping() {
        classMapping = new HashMap<>();
        classSet.forEach(c -> {
            classMapping.put(c, BeanUtils.getSuperClassNameSet(c));
        });
    }

    private void createBean() throws Exception {
        for (Class<?> c : classSet) {
            // 创建bean
            Object bean = doCreateBean(c);
        }
    }

    private void handlerMapping() {
        for (Map.Entry<String, Object> entry : singletonBeanMap.entrySet()) {
            // 映射处理器
            doHandlerMapping(entry.getValue(), proxySingletonBeanMap.get(entry.getKey()));
        }
    }


    /**
     * 映射处理器
     *
     * @param target
     * @param proxy
     */
    private void doHandlerMapping(Object target, Object proxy) {
        Class<?> c = target.getClass();
        if (!c.isAnnotationPresent(Controller.class)) {
            return;
        }
        String classUrl = "";
        if (c.isAnnotationPresent(RequestMapping.class)) {
            // 类上请求路径
            classUrl = c.getAnnotation(RequestMapping.class).value();
        }
        Handle handle = null;
        String url;
        Parameter[] parameters = null;
        Class<?> parameterType = null;
        Map<String, Integer> parameterIndexMap = null;
        List<Method> methods = Arrays.stream(c.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(RequestMapping.class)).collect(Collectors.toList());
        for (Method method : methods) {
            url = classUrl + method.getAnnotation(RequestMapping.class).value();
            parameters = method.getParameters();
            if (ArrayUtils.isNotEmpty(parameters)) {
                parameterIndexMap = new HashMap<>();
                for (int i = 0; i < parameters.length; i++) {
                    parameterType = parameters[i].getType();
                    if (parameterType.equals(HttpServletRequest.class) || parameterType.equals(HttpServletResponse.class)) {
                        // "&"防止参数重名
                        parameterIndexMap.put("&" + parameterType.getSimpleName(), i);
                    } else {
                        parameterIndexMap.put(parameters[i].getName(), i);
                    }

                }
            }
//            handle = Handle.builder().method(method).object(proxy).pattern(Pattern.compile(url)).parameterIndexMap(parameterIndexMap).build();
            handle = Handle.builder().method(method).object(target).pattern(Pattern.compile(url)).parameterIndexMap(parameterIndexMap).build();
            handleMapping.put(url, handle);
            parameterIndexMap = null;
        }
    }

    private Object doCreateBean(Class<?> c) throws Exception {
        Object bean = null;
//        Object proxyBean = proxyClassBeanMap.get(c);
        Object proxyBean = classBeanMap.get(c);
        if (proxyBean != null) {
            return proxyBean;
        }
        String beanName = BeanUtils.createBeanName(c);
        if (StringUtils.isBlank(beanName)) {
            boolean find = false;
            for (Map.Entry<Class<?>, Set<Class<?>>> entry : classMapping.entrySet()) {
                for (Class<?> supperClass : entry.getValue()) {
                    if (supperClass.equals(c)) {
                        c = entry.getKey();
                        beanName = BeanUtils.createBeanName(c);
                        find = true;
                        break;
                    }
                }
                if (find) {
                    break;
                }
            }
            if (StringUtils.isBlank(beanName)) {
                throw new RuntimeException(c.getSimpleName() + " has no valid annotation");
            }
        }
        // 创建bean
        Constructor<?> constructor = c.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        if (constructor.isAnnotationPresent(Autowired.class)) {
            // 构造器@Autowired
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            Class<?> parameterType = null;
            Object parameterBean = null;
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterType = parameterTypes[i];
//                parameterBean = proxyClassBeanMap.get(parameterType);
                parameterBean = classBeanMap.get(parameterType);
                if (parameterBean == null) {
                    parameterBean = doCreateBean(parameterType);
                }
                parameters[i] = parameterBean;
            }
            bean = constructor.newInstance(parameters);
        } else {
            bean = constructor.newInstance();
        }
        // 放入容器
        singletonBeanMap.put(beanName, bean);
        for (Class<?> superClass : BeanUtils.getSuperClassNameSet(c)) {
            classBeanMap.put(superClass, bean);
        }
        // 依赖注入
        diBean(bean);
//        // 代理bean
//        return handleProxy(beanName, bean);
        return bean;
    }

    /**
     * 代理（事务控制）
     */
    private Object handleProxy(String beanName, Object bean) throws Exception {
        // 代理工厂
        com.yujia.factory.ProxyFactory proxyFactory = getBeanByType(com.yujia.factory.ProxyFactory.class);
        if (proxyFactory == null) {
            proxyFactory = (ProxyFactory) doCreateBean(ProxyFactory.class);
        }
        // 代理bean
        Object proxyBean = proxyFactory.proxy(bean, Boolean.TRUE);
        proxySingletonBeanMap.put(beanName, proxyBean);
        for (Class<?> superClass : BeanUtils.getSuperClassNameSet(bean.getClass())) {
            proxyClassBeanMap.put(superClass, proxyBean);
        }
        return proxyBean;
    }

    /**
     * 属性注入
     */
    private void diBean(Object bean) throws Exception {
        Class<?> fieldClass = null;
        Object fieldBean = null;
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            fieldClass = field.getType();
            // @Value
            if (field.isAnnotationPresent(com.yujia.annotation.Value.class)) {
                if (fieldClass.isPrimitive() || fieldClass.getName().equals(String.class.getName())) {
                    field.set(bean, properties.get(field.getAnnotation(com.yujia.annotation.Value.class).value()));
                }
                continue;
            }
            // @Autowired
            if (field.isAnnotationPresent(com.yujia.annotation.Autowired.class)) {
                fieldBean = classBeanMap.get(fieldClass);
                if (fieldBean == null) {
                    fieldBean = doCreateBean(fieldClass);
                }
                field.set(bean, fieldBean);
                continue;
            }
        }
    }

    public static <T> T getBeanByType(Class<T> clazz) {
        return (T) classBeanMap.get(clazz);
    }

    public static <T> T getProxyBeanByType(Class<T> clazz) {
        return (T) proxyClassBeanMap.get(clazz.getName());
    }

    public static Handle getHandleByUrl(String url) {
        return handleMapping.get(url);
    }
}
