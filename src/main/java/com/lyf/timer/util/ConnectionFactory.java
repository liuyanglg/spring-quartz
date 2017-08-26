package com.lyf.timer.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private DruidDataSource dataSourceCenter;
    private DruidDataSource dataSourceCmp;

    public DruidDataSource getDataSourceCenter() {
        return dataSourceCenter;
    }

    public DruidDataSource getDataSourceCmp() {
        return dataSourceCmp;
    }

    public void setDataSourceCenter(DruidDataSource dataSourceCenter) {
        this.dataSourceCenter = dataSourceCenter;
    }

    public void setDataSourceCmp(DruidDataSource dataSourceCmp) {
        this.dataSourceCmp = dataSourceCmp;
    }

    public Connection getConnectionCenter() throws SQLException{
        return dataSourceCenter.getConnection();
    }

    public Connection getConnectionCmp() throws SQLException{
        return dataSourceCmp.getConnection();
    }
}
