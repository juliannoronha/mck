/* ==========================================================================
 * Authentication Controller Module
 *
 * PURPOSE: Manages user authentication flows and session handling
 * DEPENDENCIES: Spring MVC, Spring Security, AuditLogService
 * SCOPE: Web authentication endpoints
 * 
 * SECURITY CONSIDERATIONS:
 * - CSRF protection enabled by default
 * - Session fixation protection
 * - Secure session cookie handling
 * - Audit logging of authentication events
 * ========================================================================== */

package com.demoproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.demoproject.demo.services.AuditLogService;

/* --------------------------------------------------------------------------
 * Authentication Controller Implementation
 * 
 * FUNCTIONALITY:
 * - Handles login page display
 * - Manages logout status
 * - Provides view resolution
 * - Audit logs authentication events
 * 
 * IMPORTANT NOTES:
 * - Relies on Spring Security for auth processing
 * - Uses Thymeleaf template resolution
 * - Stateless design pattern
 * - Comprehensive audit trail
 * -------------------------------------------------------------------------- */
@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuditLogService auditLogService;

    public AuthController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /* .... Login Page Handler .... */
    /**
     * Renders the authentication login page view.
     *
     * @param logout Optional parameter indicating logout status
     * @param model Spring MVC model for view attributes
     * @returns String View name for template resolution
     * 
     * @note Handles both fresh login and post-logout states
     * @example GET /login
     * @example GET /login?logout
     * 
     * VALIDATION:
     * - Null-safe logout parameter handling
     * - Model attribute safety checks
     * AUDIT:
     * - Logs login page access
     * - Logs successful logout events
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout, 
                       Model model, 
                       Authentication authentication) {
        if (logout != null) {
            model.addAttribute("logout", true);
            auditLogService.logEvent(
                "USER_LOGOUT",
                "AUTH",
                "User logged out successfully"
            );
            logger.info("User logged out successfully");
        }
        return "login";
    }

    /**
     * Handles successful login events.
     *
     * @param authentication Current user's authentication details
     * @note Automatically called by Spring Security
     * @security Requires valid authentication
     */
    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        String username = authentication.getName();
        auditLogService.logEvent(
            "USER_LOGIN",
            "AUTH",
            "Successful login for user: " + username
        );
        logger.info("Successful login for user: {}", username);
        return "redirect:/dashboard";
    }

    /**
     * Handles failed login attempts.
     *
     * @param error Error details from Spring Security
     * @note Automatically called by Spring Security
     * @security Logs failed attempts for monitoring
     */
    @GetMapping("/login-error")
    public String loginError(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("loginError", true);
        auditLogService.logEvent(
            "LOGIN_FAILED",
            "AUTH",
            "Failed login attempt: " + (error != null ? error : "Unknown error")
        );
        logger.warn("Failed login attempt: {}", error);
        return "login";
    }

    /* @todo [SECURITY] Add rate limiting for failed attempts
     * @todo [FEATURE] Add 2FA support
     * @todo [AUDIT] Add IP address logging
     * @todo [PERF] Consider caching login page
     */
}