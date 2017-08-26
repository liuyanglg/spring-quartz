package com.lyf.timer.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

public class Log4jPropertyConfigurer extends PropertyPlaceholderConfigurer {
    private Properties properties;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory,
                                     Properties props) throws BeansException {

        super.processProperties(beanFactory, props);
        //load properties to ctxPropertiesMap
        this.properties = props;
    }

    //static method for accessing context properties

    public Properties getProperties() {
        return properties;
    }
}
