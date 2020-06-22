package com.yujia.factory;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Component;
import com.yujia.annotation.Transactional;
import com.yujia.exception.ProxyException;
import com.yujia.manager.TransactionManager;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 代理工厂
 */
@Component
public class ProxyFactory {

    @Autowired
    private TransactionManager transactionManager;

    public <T> T proxy(T t) {
        // 默认cglib代理
        return proxy(t, null);
    }

    public <T> T proxy(T t, Boolean jdk) {
        if (t == null) {
            throw new NullPointerException("t can not be null");
        }
        Class<?>[] interfaces = t.getClass().getInterfaces();
        if (interfaces.length == 0) {
            // 没有父接口，执行cglib代理
            return cglibProxy(t);
        }
        if (!Boolean.TRUE.equals(jdk)) {
            return cglibProxy(t);
        }
        return jdkProxy(t);
    }

    private <T> T jdkProxy(T t) {
        Class<?> tClass = t.getClass();
        Class<?>[] interfaces = tClass.getInterfaces();
        // 校验是否拥有父接口
        if (interfaces.length == 0) {
            throw ProxyException.build(t.getClass().toString() + " has no interfaces");
        }
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), interfaces, new JdkInvocationHandler(t));
    }

    private class JdkInvocationHandler implements InvocationHandler {

        private Object target;

        private boolean classIsTransactional;

        public JdkInvocationHandler(Object target) {
            this.target = target;
            this.classIsTransactional = target.getClass().isAnnotationPresent(Transactional.class);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object res = null;
            // 校验是否声明式事务
            if (classIsTransactional || method.isAnnotationPresent(Transactional.class)) {
                try {
                    transactionManager.begin();
                    res = method.invoke(target, args);
                    transactionManager.commit();
                } catch (Exception e) {
                    transactionManager.rollback();
                    throw e;
                }
            } else {
                res = method.invoke(target, args);
            }
            return res;
        }
    }

    public <T> T cglibProxy(T t) {
        return (T) Enhancer.create(t.getClass(), new CglibInvocationHandler(t));
    }

    private class CglibInvocationHandler implements net.sf.cglib.proxy.InvocationHandler {

        private Object target;

        private boolean classIsTransactional;

        public CglibInvocationHandler(Object target) {
            this.target = target;
            this.classIsTransactional = target.getClass().isAnnotationPresent(Transactional.class);
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            Object res = null;
            // 校验是否声明式事务
            if (classIsTransactional || method.isAnnotationPresent(Transactional.class)) {
                try {
                    transactionManager.begin();
                    res = method.invoke(target, objects);
                    transactionManager.commit();
                } catch (Exception e) {
                    transactionManager.rollback();
                    throw e;
                }
            } else {
                res = method.invoke(target, objects);
            }
            return res;
        }
    }
}
