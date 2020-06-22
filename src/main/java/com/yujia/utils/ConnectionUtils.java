package com.yujia.utils;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Component;
import com.yujia.utils.DruidUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接
 */
@Component("connectionUtils")
public class ConnectionUtils {

    private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    @Autowired
    private DruidUtils druidUtils;

    public Connection getConnection() throws SQLException {
        Connection connection = connectionThreadLocal.get();
        if (connection == null) {
            connection = druidUtils.getConnection();
            connectionThreadLocal.set(connection);
        }
        return connection;
    }
}
