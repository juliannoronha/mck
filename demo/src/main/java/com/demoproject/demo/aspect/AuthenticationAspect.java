package com.demoproject.demo.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for handling authentication checks.
 * This aspect intercepts methods annotated with @RequiresAuthentication
 * and ensures that the user is properly authenticated before allowing
 * the method execution to proceed.
 */
@Aspect
@Component
public class AuthenticationAspect {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationAspect.class);

    /**
     * Checks if the current user is authenticated.
     * This method is executed before any method annotated with @RequiresAuthentication.
     *
     * @throws IllegalStateException if the user is not authenticated
     */
    @Before("@annotation(com.demoproject.demo.annotation.RequiresAuthentication)")
    public void checkAuthentication() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                throw new IllegalStateException("User is not authenticated");
            }
            
            logger.debug("Authentication check passed for user: {}", auth.getName());
        } catch (Exception e) {
            logger.error("Authentication check failed", e);
            throw e;
        } finally {
            // Clear authentication context to prevent memory leaks
            SecurityContextHolder.clearContext();
        }
    }
}