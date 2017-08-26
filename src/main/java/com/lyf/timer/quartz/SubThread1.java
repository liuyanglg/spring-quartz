package com.lyf.timer.quartz;


import com.lyf.timer.util.DBManager;
import com.lyf.timer.util.DataSourceEnum;
import com.lyf.timer.util.JdbcUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Package : com.aisino.admin.timer.quartz
 * @Class : SubThread1
 * @Description : 处理用户中心新增数据的线程类
 * @Author : liuya
 * @CreateDate : 2017-08-23 星期三 18:24:43
 * @Version : V1.0.0
 * @Copyright : 2017 liuya Inc. All rights reserved.
 */
public class SubThread1 extends Thread {
    public static Logger log = Logger.getLogger(SubThread1.class.getName());

    private String querySql;
    private String insertSql;
    private int offset;
    private int pageSize;
    private int taskSize;
    private String[] keys;

    public SubThread1() {
    }

    public SubThread1(String querySql, String insertSql, int offset, int pageSize, int taskSize, String[] keys) {
        this.querySql = querySql;
        this.insertSql = insertSql;
        this.offset = offset;
        this.pageSize = pageSize;
        this.taskSize = taskSize;
        this.keys = keys;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTaskSize() {
        return taskSize;
    }

    public void setTaskSize(int taskSize) {
        this.taskSize = taskSize;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    @Override
    public void run() {
        Connection cenConnection = null;
        Connection cmpConnection = null;
        String threadName = "[" + Thread.currentThread().getName() + "] ";
        try {
//            System.out.println(threadName + "start......");
            log.info(threadName+"start......");
            cenConnection = DBManager.getConnection(DataSourceEnum.CENTER);
            cmpConnection = DBManager.getConnection(DataSourceEnum.CMP);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int pages = 0;//分页的页数
        try {
            pages = taskSize / pageSize;
            if (taskSize % pageSize != 0) {
                pages += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分页大小为0，导致除数为0引起的异常");
        }

        for (int i = 0; i < pages; i++) {
            int innerOffset = offset + i * pageSize;
            try {
                List<Map<String, Object>> queryList = null;
                if (querySql != null && querySql.trim().length() > 0) {
                    queryList = JdbcUtils.queryPage(querySql, cenConnection, innerOffset, pageSize);//查询用户中心新增的数据
                }
                if (insertSql != null && insertSql.trim().length() > 0) {
                    JdbcUtils.insertBatch(insertSql, cmpConnection, queryList, keys);//将关联关系插入到关系表
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JdbcUtils.close(cenConnection);
        JdbcUtils.close(cmpConnection);

        TimerTaskJob.countDown(threadName);//线程计数器减1
    }
}
