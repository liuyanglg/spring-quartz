package com.lyf.timer.main;

import com.lyf.timer.util.SpringApplication;
import org.apache.log4j.Logger;

public class StartMain {
    public static Logger log = Logger.getLogger(StartMain.class.getName());

    public static void main(String[] args) {
        SpringApplication.initSpring();
        log.info("Timer has launched.");
    }
}
