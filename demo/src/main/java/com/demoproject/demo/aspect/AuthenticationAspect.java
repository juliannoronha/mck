package com.demoproject.demo.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

/**
 * Aspect for handling authentication checks.
 * This aspect intercepts methods annotated with @RequiresAuthentication
 * and ensures that the user is properly authenticated before allowing
 * the method execution to proceed.
 */
@Aspect
@Component
public class AuthenticationAspect {

    /**
     * Checks if the current user is authenticated.
     * This method is executed before any method annotated with @RequiresAuthentication.
     *
     * @throws IllegalStateException if the user is not authenticated
     */
    @Before("@annotation(com.demoproject.demo.annotation.RequiresAuthentication)")
    public void checkAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if authentication is null or if it's an instance of AnonymousAuthenticationToken
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("User is not authenticated");
        }
        
        // TODO: Consider adding logging for failed authentication attempts
    }
}