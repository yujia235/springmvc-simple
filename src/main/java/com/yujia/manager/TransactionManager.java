package com.yujia.manager;

import com.yujia.annotation.Autowired;
import com.yujia.annotation.Component;
import com.yujia.utils.ConnectionUtils;

import java.sql.SQLException;

/**
 * 事务管理
 */
@Component
public class TransactionManager {

    @Autowired
    private ConnectionUtils connectionUtils;

    /**
     * 设置：自动提交为false，即：开启手动事务控制
     *
     * @throws SQLException
     */
    public void begin() throws SQLException {
        connectionUtils.getConnection().setAutoCommit(false);
    }

    /**
     * 提交事务
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        connectionUtils.getConnection().commit();
    }

    /**
     * 回滚事务
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connectionUtils.getConnection().rollback();
    }
}
