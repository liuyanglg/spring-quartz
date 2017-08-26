package com.lyf.timer.quartz;

import com.lyf.timer.util.DBManager;
import com.lyf.timer.util.DataSourceEnum;
import com.lyf.timer.util.JdbcUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Package : com.aisino.admin.timer.quartz
 * @Class : SubThread2
 * @Description : 处理正式库和审核库新增数据的线程类
 * @Author : liuya
 * @CreateDate : 2017-08-23 星期三 18:20:59
 * @Version : V1.0.0
 * @Copyright : 2017 liuya Inc. All rights reserved.
 */
public class SubThread2 extends Thread {
    public static Logger log = Logger.getLogger(SubThread2.class.getName());

    private String[] querySql;
    private String insertSql;
    private int offset;
    private int pageSize;
    private int taskSize;
    private String[][] keys;

    public SubThread2() {
    }

    public SubThread2(String[] querySql, String insertSql, int offset, int pageSize, int taskSize, String[][] keys) {
        this.querySql = querySql;
        this.insertSql = insertSql;
        this.offset = offset;
        this.pageSize = pageSize;
        this.taskSize = taskSize;
        this.keys = keys;
    }

    public String[] getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String[] querySql) {
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

    public String[][] getKeys() {
        return keys;
    }

    public void setKeys(String[][] keys) {
        this.keys = keys;
    }

    @Override
    public void run() {
        Connection cenConnection = null;
        Connection cmpConnection = null;
        String threadName = "[" + Thread.currentThread().getName() + "] ";
        try {
//            System.out.println(threadName+"start......");
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
//            System.out.println("分页大小为0，导致除数为0引起的异常");
            log.error("分页大小为0，导致除数为0引起的异常");
        }

        for (int i = 0; i < pages; i++) {
            int innerOffset = offset + i * pageSize;
            try {
                List<Map<String, Object>> queryList1 = null;
                if (querySql[0] != null && querySql[0].trim().length() > 0) {
                    long t = System.currentTimeMillis();
                    queryList1 = JdbcUtils.queryPage(querySql[0], cmpConnection, innerOffset, pageSize);//查询正式库或审核库新增数据
                }
                List<Map<String, Object>> queryList2 = new ArrayList<Map<String, Object>>();
                for(Map<String, Object>map:queryList1) {
                    int len = keys[0].length;
                    if(len==3) {
                        if (map.get("code") == null) {
                            keys[0][len - 1] = "taxid";
                        } else {
                            keys[0][len - 1] = "taxidM";
                        }
                    }
                    List<Map<String, Object>>singleList = JdbcUtils.queryObject(querySql[1], cenConnection, map, keys[0]);//从用户中心查询出关联关系
                    if(singleList.size()>0){
                        queryList2.addAll(singleList);
                    }

                }
                if (insertSql != null && insertSql.trim().length() > 0) {
                    JdbcUtils.insertBatch(insertSql, cmpConnection, queryList2, keys[1]);//将关联关系插入关系表
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JdbcUtils.close(cenConnection);
        JdbcUtils.close(cmpConnection);
        TimerTaskJob.countDown(threadName);
    }
}
