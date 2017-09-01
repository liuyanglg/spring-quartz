package com.lyf.timer.util;

import java.sql.Connection;
import java.sql.SQLException;


public class DBManager {
    public static DBManager dbManager;
    private  ConnectionFactory connectionFactory;

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private  void init(){
        dbManager = this;
    }


    public static Connection getConnection(DataSourceEnum connectionName) throws SQLException{
        Connection connection=null;

        switch (connectionName){
            case CENTER:
                connection = dbManager.connectionFactory.getConnectionCenter();
                break;
            case CMP:
                connection = dbManager.connectionFactory.getConnectionCmp();
                break;
        }
        return connection;
    }

    public static void closeDataSource() throws SQLException{
        dbManager.connectionFactory.getConnectionCenter().close();
        dbManager.connectionFactory.getConnectionCenter().close();
    }

}
