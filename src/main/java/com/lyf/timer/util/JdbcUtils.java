package com.lyf.timer.util;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Package : com.aisino.mysql.uitls
 * @Class : JdbcUtils
 * @Description :
 * @Author : liuyang
 * @CreateDate : 2017-08-18 星期五 22:00:54
 * @Version : V1.0.0
 * @Copyright : 2017 liuyang Inc. All rights reserved.
 */
public class JdbcUtils {
    public static Logger log = Logger.getLogger(JdbcUtils.class.getName());

    /**
     * @Method : getConnection
     * @Description : 获取jdbc数据库连接
     * @param driver : 
     * @param url : 
     * @param username : 
     * @param password : 
     * @Return : java.sql.Connection
     * @Author : liuyang
     * @CreateDate : 2017-08-18 星期五 22:01:11
     */
    public static Connection getConnection(String driver, String url, String username, String password) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, username, password);
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            log.error("数据库连接异常：" + url);
        }
        return null;
    }

    /**
     * @Method : queryObject
     * @Description : 查询SQL返回对象列表
     * @param sql :
     * @param connection :
     * @param params : SQL占位符参数
     * @param keys :  SQL占位符参数的键值，用与获取map参数中的value，并保证参数顺序
     * @return : java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:33:12
     */
    public static List<Map<String, Object>> queryObject(String sql, Connection connection, Map<String, Object> params, String[] keys) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSetMetaData rsmd = null;
        ResultSet rs = null;
        int columns;
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < keys.length; i++) {
                Object value = params.get(keys[i]);
                ps.setObject(i + 1, value);
            }
            rs = ps.executeQuery();
            rsmd = rs.getMetaData();
            columns = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < columns; i++) {
                    map.put(rsmd.getColumnLabel(i + 1), getValueByType(rs, rsmd.getColumnType(i + 1), rsmd.getColumnLabel(i + 1)));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
        }
        return list;
    }

    /**
     * @Method : queryPage
     * @Description : 分页查询
     * @param sql :
     * @param connection :
     * @param offset : 偏移量
     * @param pageSize :  分页大小
     * @return : java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:35:23
     */
    public static List<Map<String, Object>> queryPage(String sql, Connection connection, int offset, int pageSize) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSetMetaData rsmd = null;
        ResultSet rs = null;
        int columns;
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            rs = ps.executeQuery();
            rsmd = rs.getMetaData();
            columns = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < columns; i++) {
                    map.put(rsmd.getColumnLabel(i + 1), getValueByType(rs, rsmd.getColumnType(i + 1), rsmd.getColumnLabel(i + 1)));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
        }
        return list;
    }

    /**
     * @Method : insertBatch
     * @Description : 批量插入数据，此函数不具有一般通用性
     * @param sql :
     * @param connection :
     * @param params : 批量插入的参数列表
     * @param keys :  SQL占位符参数的键值，用与获取map参数中的value，并保证参数顺序
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:36:31
     */
    public static void insertBatch(String sql, Connection connection, List<Map<String, Object>> params, String[] keys) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            int count = 0;
            for (Map<String, Object> param : params) {
                for (int i = 0; i < keys.length; i++) {
                    String value = (String) param.get(keys[i]);
                    ps.setString(i + 1, value);
                }
                ps.addBatch();
                count++;
                if (count % 500 == 0) {//数量达到500后提交一次
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            ps.executeBatch();//批量更新
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(ps);
        }
    }

    /**
     * @Method : count
     * @Description : select count查询获取数据数量
     * @param sql :
     * @param connection :
     * @return : int
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:37:45
     */
    public static int count(String sql, Connection connection) {
        int size = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                size = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
        }
        return size;
    }


    /**
     * @Method : getValueByType
     * @Description : 将执行的结果集进行类型转化选择
     * @param rs :
     * @param type :
     * @param name :
     * @Return : java.lang.Object
     * @Author : liuyang
     * @CreateDate : 2017-08-18 星期五 22:04:54
     */
    private static Object getValueByType(ResultSet rs, int type, String name) throws SQLException {

        switch (type) {
            case Types.NUMERIC:
                return rs.getLong(name);
            case Types.VARCHAR:
                return rs.getString(name);
            case Types.DATE:
                return rs.getDate(name);
            case Types.TIMESTAMP:
                return rs.getTimestamp(name).toString().substring(0, rs.getTimestamp(name).toString().length() - 2);
            case Types.INTEGER:
                return rs.getInt(name);
            case Types.DOUBLE:
                return rs.getDouble(name);
            case Types.FLOAT:
                return rs.getFloat(name);
            case Types.BIGINT:
                return rs.getLong(name);
            default:
                return rs.getObject(name);
        }
    }

    public static void close(Connection x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close connection error", e);
        }
    }

    public static void close(Statement x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close statement error", e);
        }
    }

    public static void close(ResultSet x) {
        if (x == null) {
            return;
        }
        try {
            x.close();
        } catch (Exception e) {
            log.debug("close result set error", e);
        }
    }
}
