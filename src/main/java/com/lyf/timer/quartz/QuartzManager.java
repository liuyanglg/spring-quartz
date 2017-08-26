package com.lyf.timer.quartz;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {
    public static Logger log = Logger.getLogger(QuartzManager.class.getName());

    private static SchedulerFactory sf = new StdSchedulerFactory();
    private static String JOB_GROUP_NAME = "job_group";
    private static String TRIGGER_NAME = "trigger";
    private static String TRIGGER_GROUP_NAME = "trigger_group";

    /**
     * @Method : addJob
     * @Description : 添加一个定时任务
     * @param jobName :
     * @param clazz :
     * @param time :
     * @Return : void
     * @Author : liuyang
     * @CreateDate : 2017-08-19 星期六 11:43:49
     */
    public static void addJob(String jobName, Class clazz, String time) {
        try {
            Scheduler scheduler = sf.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(jobName, JOB_GROUP_NAME).build();//任务名，任务组，任务执行类
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_NAME, TRIGGER_GROUP_NAME)
                    .withSchedule(CronScheduleBuilder.cronSchedule(time)).build();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }
}
