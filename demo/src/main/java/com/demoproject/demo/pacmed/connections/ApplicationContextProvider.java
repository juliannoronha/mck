package com.demoproject.demo.connections;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext context;
    
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        context = ac;
    }
    
    public static ApplicationContext getApplicationContext() {
        return context;
    }
}