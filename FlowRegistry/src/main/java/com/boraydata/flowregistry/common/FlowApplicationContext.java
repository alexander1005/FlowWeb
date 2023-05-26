package com.boraydata.flowregistry.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @className: FlowApplicationContext
 * @description: TODO
 * @author: hatter
 * @date: 2021/4/23
 **/
public enum FlowApplicationContext {

    CONTEXT;

    private ClassPathXmlApplicationContext context;

    FlowApplicationContext() {
        context = new ClassPathXmlApplicationContext("application.xml");
    }

    public ApplicationContext getContext() {
        return context;
    }

}
