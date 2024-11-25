package com.demoproject.demo.connections;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConnectionLeakMonitorAspect {
    
    private static final Logger logger = Logger.getLogger(ConnectionLeakMonitorAspect.class.getName());
    
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitorConnectionLeak(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.severe(String.format(
                "Potential connection leak detected in %s. Method execution time: %dms. Error: %s. Stack trace: %s", 
                joinPoint.getSignature().toShortString(),
                System.currentTimeMillis() - startTime,
                e.getMessage(),
                getStackTraceAsString(e)
            ));
            throw e;
        }
    }
    
    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
