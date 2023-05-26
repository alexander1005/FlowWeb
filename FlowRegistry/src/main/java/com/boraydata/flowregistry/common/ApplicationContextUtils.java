package com.boraydata.flowregistry.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @date: 2021/4/20
 * @author: hatter
 **/
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        applicationContext = appContext;
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            String msg = "The applicationContext is not yet available. "
                    + "Please ensure that the spring applicationContext is completly created before calling this method!";
            throw new IllegalStateException(msg);
        }

        return applicationContext;
    }

    public static <T> T getBean(Class<T> type) {
        if (applicationContext == null) {
            String msg = "The applicationContext is not yet available. "
                    + "Please ensure that the spring applicationContext is completly created before calling this method!";
            throw new IllegalStateException(msg);
        }
        return applicationContext.getBean(type);
    }
}
