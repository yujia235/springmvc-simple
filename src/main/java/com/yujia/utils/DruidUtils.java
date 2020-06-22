package com.yujia.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.yujia.annotation.Autowired;
import com.yujia.annotation.Component;
import com.yujia.config.DataSourceConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据源
 * 为防止数据源被外界误修改，故数据源不对外暴露，仅提供获取连接的方法
 */
@Component
public class DruidUtils {

    private DruidDataSource druidDataSource;

    @Autowired
    private DruidUtils(DataSourceConfig config) {
        druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(config.getUrl());
        druidDataSource.setUsername(config.getUsername());
        druidDataSource.setPassword(config.getPassword());
        druidDataSource.setDriverClassName(config.getDriverClassName());
    }

//    static {
//        druidDataSource.setUrl("jdbc:mysql:///0db");
//        druidDataSource.setUsername("root");
//        druidDataSource.setPassword("root");
//        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
//    }

    public Connection getConnection() throws SQLException {
        return druidDataSource.getConnection();
    }
}
