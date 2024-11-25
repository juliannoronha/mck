package com.demoproject.demo.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
    private final MeterRegistry meterRegistry;

    public AuthenticationAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Checks if the current user is authenticated.
     * This method is executed before any method annotated with @RequiresAuthentication.
     *
     * @throws IllegalStateException if the user is not authenticated
     */
    @Before("@annotation(com.demoproject.demo.annotation.RequiresAuthentication)")
    public void checkAuthentication() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                // Increment counter for failed authentications
                meterRegistry.counter("auth.check.failures").increment();
                logger.warn("Authentication check failed: User is not authenticated");
                throw new IllegalStateException("User is not authenticated");
            }
            
            // Record successful authentication
            meterRegistry.counter("auth.check.success").increment();
            logger.debug("Authentication check passed for user: {}", auth.getName());
        } catch (Exception e) {
            // Record authentication errors
            meterRegistry.counter("auth.check.errors", "error", e.getClass().getSimpleName()).increment();
            logger.error("Authentication check failed: {}", e.getMessage());
            throw e;
        } finally {
            // Record the timing of the authentication check
            sample.stop(meterRegistry.timer("auth.check.time"));
        }
    }
}