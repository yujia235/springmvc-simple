package com.yujia.config;

import com.yujia.annotation.Component;
import com.yujia.annotation.Value;
import lombok.Data;

@Data
@Component
public class DataSourceConfig {
    @Value("jdbc.connection.url")
    private String url;
    @Value("jdbc.connection.username")
    private String username;
    @Value("jdbc.connection.password")
    private String password;
    @Value("jdbc.connection.driver_class_name")
    private String driverClassName;
}
