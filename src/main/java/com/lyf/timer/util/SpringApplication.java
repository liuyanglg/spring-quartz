package com.lyf.timer.util;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

public class SpringApplication {
    public static ApplicationContext context;
    public  static void initSpring(){
        if(context==null){
            context = new ClassPathXmlApplicationContext("classpath*:/spring/spring.xml");
        }
        initLog4j();
    }

    public static ApplicationContext getContext() {
        if(context==null){
            context = new ClassPathXmlApplicationContext("classpath*:/spring/spring.xml");
        }
        return context;
    }

    public static void initLog4j(){
        Log4jPropertyConfigurer log4jPropertyConfigurer = (Log4jPropertyConfigurer) context.getBean("log4jPropertyConfigurer");
        Properties log4jProperties = log4jPropertyConfigurer.getProperties();
        PropertyConfigurator.configure(log4jProperties);//设置Log4j配置
    }
}
