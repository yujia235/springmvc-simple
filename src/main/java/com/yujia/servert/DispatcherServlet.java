package com.yujia.servert;

import com.yujia.factory.BeanFactory1;
import com.yujia.factory.model.Handle;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            new BeanFactory1(this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("DispatcherServlet init error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Handle handle = BeanFactory1.getHandleByUrl(req.getRequestURI());
        if (handle == null) {
//            throw new ServletException("404 not found : " + req.getRequestURI());
            resp.getWriter().write("404 not found");
        }
        Object[] parameters = new Object[handle.getMethod().getParameters().length];
        Integer index = null;
        String value = null;
        Map<String, Integer> parameterIndexMap = handle.getParameterIndexMap();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            index = parameterIndexMap.get(entry.getKey());
            if (index == null) {
                continue;
            }
            value = StringUtils.join(entry.getValue(), ",");
            parameters[index.intValue()] = value;
        }
        // 设置HttpServletRequest
        index = parameterIndexMap.get("&" + HttpServletRequest.class.getSimpleName());
        if (index != null) {
            parameters[index.intValue()] = req;
        }
        // 设置HttpServletResponse
        index = parameterIndexMap.get("&" + HttpServletResponse.class.getSimpleName());
        if (index != null) {
            parameters[index.intValue()] = resp;
        }

        // 执行
        try {
            Object invoke = handle.getMethod().invoke(handle.getObject(), parameters);
            resp.getWriter().write(invoke.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
