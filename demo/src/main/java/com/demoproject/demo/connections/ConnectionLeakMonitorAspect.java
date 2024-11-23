package com.demoproject.demo.connections;

import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@Aspect
@Component
public class ConnectionLeakMonitorAspect {
    
    private static final Logger logger = Logger.getLogger(ConnectionLeakMonitorAspect.class.getName());
    
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object monitorConnectionLeak(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.severe(String.format("Potential connection leak detected in %s: %s", 
                joinPoint.getSignature().toShortString(), 
                e.getMessage()));
            throw e;
        } finally {
            // Ensure connection is released
            EntityManagerFactory emf = EntityManagerFactoryUtils.findEntityManagerFactory(
                ApplicationContextProvider.getApplicationContext(), 
                "entityManagerFactory");
            if (emf != null) {
                EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        }
    }
}
