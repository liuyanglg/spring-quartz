package com.lyf.timer.quartz;


import com.lyf.timer.util.DBManager;
import com.lyf.timer.util.DataSourceEnum;
import com.lyf.timer.util.JdbcUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.sql.Connection;
import java.sql.SQLException;

import static com.lyf.timer.constants.SqlQuery.*;


/**
 * @Package : com.aisino.admin.timer.quartz
 * @Class : TimerTaskJob
 * @Description : 包含定时任务的类
 * @Author : liuya
 * @CreateDate : 2017-08-23 星期三 18:27:58
 * @Version : V1.0.0
 * @Copyright : 2017 liuya Inc. All rights reserved.
 */
public class TimerTaskJob extends QuartzJobBean {
    public static Logger log = Logger.getLogger(TimerTaskJob.class.getName());

    public static volatile int threadCounter = 0;
    public static volatile int init = 1;
    private static Connection centerConnection = null;
    private static Connection cmpConnection = null;
    private int pageSize = 500;


    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @Method : executeInternal
     * @Description : quartz 定时任务启动函数
     * @param jobExecutionContext :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:04:59
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        initParams();
        Long timeStart1 = System.currentTimeMillis();

        int batchThreadNum = 5;
        threadCounter += 2 * batchThreadNum;
        if(init>0) {
            updateInitAudit(pageSize, batchThreadNum);
        }else {
        /*更新用户中心每天新增的数据*/
            updateAddUserServiceForA(pageSize, batchThreadNum);

        /*审核库中每天新增的数据*/
            updateAddAudit(pageSize, batchThreadNum);
        }
            destroy();//释放连接池
            Long timeEnd1 = System.currentTimeMillis();

        log.info("更新完毕，耗时为：" + getTimeString(timeEnd1 - timeStart1));
    }

    /**
     * @Method : initParams
     * @Description : 初始化数据库连接池
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:05:52
     */
    public void initParams() {
        log.info("更新任务开始......");
    }


    /**
     * @Method : getTimeString
     * @Description : 将毫秒转换为小时
     * @Param useTime :
     * @ReturnType : java.lang.String
     * @Author : liuyang
     * @CreateDate : 2017-08-19 星期六 19:52:18
     */
    public String getTimeString(Long useTime) {
        StringBuffer timeBuffer = new StringBuffer();
        long ms = useTime % 1000;
        long sec = (useTime / (1000)) % (60);
        long min = (useTime / (1000 * 60)) % (60);
        long hour = useTime / (1000 * 60 * 60);
        if (hour > 0) {
            timeBuffer.append(hour + "时");
        }
        if (min > 0) {
            timeBuffer.append(min + "分");
        }
        if (sec > 0) {
            timeBuffer.append(sec + "秒");
        }
        timeBuffer.append(ms + "毫秒");
        return timeBuffer.toString();
    }

    public void destroy() {
        while (threadCounter > 0) {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            DBManager.closeDataSource();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Method : countDown
     * @Description : 线程同步计数器
     * @param threadName :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:06:30
     */
    public static void countDown(String threadName) {
        threadCounter--;
//        System.out.println(threadName +" has finished, the remain number of running thread  is:" + threadCounter);
        log.info(threadName + " has finished,  remain  running thread :" + threadCounter);
    }

    /**
     * @Method : updateAddAudit
     * @Description : 将审核库每天新增的数据与用户中心前一天的所有数据建立关系，
     * 如果与用户中心的全部数据关联，用户中心和审核库每天增的数据将重复建立关系，下同
     * @param pageSize :
     * @param threadNum :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:10:05
     */
    public void updateInitAudit(int pageSize, int threadNum) {
        int totalSize = 0;
        try {
            cmpConnection = DBManager.getConnection(DataSourceEnum.CMP);
            totalSize = JdbcUtils.count(SQL_QUERY_NEW_TB_A_COUNT_INIT, cmpConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(cmpConnection);
        }

        String[][] keys = {{"code", "taxid", "taxidM"}, {"code", "taxid", "serviceid"}};//SQL语句占位符
        int taskSize = 0;//每个线程处理的任务量
        int remain = 0;
        if (totalSize % (pageSize * threadNum) == 0) {
            taskSize = totalSize / threadNum;
        } else {
            remain = totalSize % (pageSize * threadNum);
            taskSize = (totalSize - remain) / threadNum;
        }

        for (int i = 0; i < threadNum; i++) {
            SubThread2 subThread = new SubThread2();
            String[] querySqlArray = {SQL_QUERY_NEW_TB_A_INIT, SQL_QUERY_OLD_TB_US_FOR_A_INIT};
            subThread.setQuerySql(querySqlArray);
            subThread.setInsertSql(SQL_INSERT_NEW_TB_RA);
            if (i < threadNum - 1) {
                subThread.setTaskSize(taskSize);
            } else {
                subThread.setTaskSize(taskSize + remain);
            }
            subThread.setOffset(i * taskSize);
            subThread.setPageSize(pageSize);
            subThread.setKeys(keys);
            subThread.start();
        }
    }

    /**
     * @Method : updateAddUserServiceForA
     * @Description : 将用户中心每天新增的数据与审核库建立关系
     * @param pageSize :
     * @param threadNum :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:07:07
     */
    public void updateAddUserServiceForA(int pageSize, int threadNum) {
        int totalSize = 0;
        try {
            centerConnection = DBManager.getConnection(DataSourceEnum.CENTER);
            totalSize = JdbcUtils.count(SQL_QUERY_NEW_TB_US_COUNT, centerConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(centerConnection);
        }

        String[] keys = {"c_serviceid", "c_taxnum", "c_taxnum"};//sql语句占位符
        int taskSize = 0;
        int remain = 0;
        if (totalSize % (pageSize * threadNum) == 0) {
            taskSize = totalSize / threadNum;
        } else {
            remain = totalSize % (pageSize * threadNum);
            taskSize = (totalSize - remain) / threadNum;
        }

        for (int i = 0; i < threadNum; i++) {
            SubThread1 subThread = new SubThread1();
            subThread.setQuerySql(SQL_QUERY_NEW_TB_US);
            subThread.setInsertSql(SQL_INSERT_TB_RA);
            if (i < threadNum - 1) {
                subThread.setTaskSize(taskSize);
            } else {
                subThread.setTaskSize(taskSize + remain);
            }
            subThread.setOffset(i * taskSize);
            subThread.setPageSize(pageSize);
            subThread.setKeys(keys);
            subThread.start();
        }
    }

    /**
     * @Method : updateAddUserServiceForM
     * @Description : 将用户中心每天新增的数据与正式库建立关系
     * @param pageSize :
     * @param threadNum :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:08:56
     */
    public void updateAddUserServiceForM(int pageSize, int threadNum) {
        int totalSize = 0;
        try {
            centerConnection = DBManager.getConnection(DataSourceEnum.CENTER);
            totalSize = JdbcUtils.count(SQL_QUERY_NEW_TB_US_COUNT, centerConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(centerConnection);
        }

        String[] keys = {"c_serviceid", "c_taxnum"};//SQL语句占位符
        int taskSize = 0;
        int remain = 0;
        if (totalSize % (pageSize * threadNum) == 0) {
            taskSize = totalSize / threadNum;
        } else {
            remain = totalSize % (pageSize * threadNum);
            taskSize = (totalSize - remain) / threadNum;
        }

        for (int i = 0; i < threadNum; i++) {
            SubThread1 subThread = new SubThread1();
            subThread.setQuerySql(SQL_QUERY_NEW_TB_US);
            subThread.setInsertSql(SQL_INSERT_TB_RM);
            if (i < threadNum - 1) {
                subThread.setTaskSize(taskSize);
            } else {
                subThread.setTaskSize(taskSize + remain);
            }
            subThread.setOffset(i * taskSize);
            subThread.setPageSize(pageSize);
            subThread.setKeys(keys);
            subThread.start();
        }
    }

    /**
     * @Method : updateAddAudit
     * @Description : 将审核库每天新增的数据与用户中心前一天的所有数据建立关系，
     * 如果与用户中心的全部数据关联，用户中心和审核库每天增的数据将重复建立关系，下同
     * @param pageSize :
     * @param threadNum :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:10:05
     */
    public void updateAddAudit(int pageSize, int threadNum) {
        int totalSize = 0;
        try {
            cmpConnection = DBManager.getConnection(DataSourceEnum.CMP);
            totalSize = JdbcUtils.count(SQL_QUERY_NEW_TB_A_COUNT, cmpConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(cmpConnection);
        }

        String[][] keys = {{"code", "taxid", "taxidM"}, {"code", "taxid", "serviceid"}};//SQL语句占位符
        int taskSize = 0;//每个线程处理的任务量
        int remain = 0;
        if (totalSize % (pageSize * threadNum) == 0) {
            taskSize = totalSize / threadNum;
        } else {
            remain = totalSize % (pageSize * threadNum);
            taskSize = (totalSize - remain) / threadNum;
        }

        for (int i = 0; i < threadNum; i++) {
            SubThread2 subThread = new SubThread2();
            String[] querySqlArray = {SQL_QUERY_NEW_TB_A, SQL_QUERY_OLD_TB_US_FOR_A};
            subThread.setQuerySql(querySqlArray);
            subThread.setInsertSql(SQL_INSERT_NEW_TB_RA);
            if (i < threadNum - 1) {
                subThread.setTaskSize(taskSize);
            } else {
                subThread.setTaskSize(taskSize + remain);
            }
            subThread.setOffset(i * taskSize);
            subThread.setPageSize(pageSize);
            subThread.setKeys(keys);
            subThread.start();
        }
    }

    /**
     * @Method : updateAddMaintain
     * @Description : 将正式库每天新增的数据与用户中心前一天的所有数据建立关系
     * @param pageSize :
     * @param threadNum :
     * @return : void
     * @author : liuya
     * @CreateDate : 2017-08-23 星期三 18:12:44
     */
    public void updateAddMaintain(int pageSize, int threadNum) {
        int totalSize = 0;
        try {
            cmpConnection = DBManager.getConnection(DataSourceEnum.CMP);
            totalSize = JdbcUtils.count(SQL_QUERY_NEW_TB_M_COUNT, cmpConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(cmpConnection);
        }

        String[][] keys = {{"code", "taxid"}, {"code", "serviceid"}};//SQL语句占位符
        int taskSize = 0;
        int remain = 0;
        if (totalSize % (pageSize * threadNum) == 0) {
            taskSize = totalSize / threadNum;
        } else {
            remain = totalSize % (pageSize * threadNum);
            taskSize = (totalSize - remain) / threadNum;
        }

        for (int i = 0; i < threadNum; i++) {
            SubThread2 subThread = new SubThread2();
            String[] querySqlArray = {SQL_QUERY_NEW_TB_M, SQL_QUERY_OLD_TB_US_FOR_M};
            subThread.setQuerySql(querySqlArray);
            subThread.setInsertSql(SQL_INSERT_NEW_TB_RM);
            if (i < threadNum - 1) {
                subThread.setTaskSize(taskSize);
            } else {
                subThread.setTaskSize(taskSize + remain);
            }
            subThread.setOffset(i * taskSize);
            subThread.setPageSize(pageSize);
            subThread.setKeys(keys);
            subThread.start();
        }

    }
}
