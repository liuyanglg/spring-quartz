package com.lyf.timer.util;

import org.springframework.context.ApplicationContext;

import java.sql.Connection;
import java.sql.SQLException;


public class DBManager {
    private static ConnectionFactory connectionFactory;

    public static ConnectionFactory getConnectionFactory() {
        ApplicationContext context = SpringApplication.getContext();
        connectionFactory = (ConnectionFactory) context.getBean("connectionFactory");
        return connectionFactory;
    }

    public static Connection getConnection(DataSourceEnum connectionName) throws SQLException{
        Connection connection=null;

        switch (connectionName){
            case CENTER:
                connection = getConnectionFactory().getConnectionCenter();
                break;
            case CMP:
                connection = getConnectionFactory().getConnectionCmp();
                break;
        }
        return connection;
    }

    public static void closeDataSource() throws SQLException{
        connectionFactory.getConnectionCenter().close();
        connectionFactory.getConnectionCenter().close();
    }

}
