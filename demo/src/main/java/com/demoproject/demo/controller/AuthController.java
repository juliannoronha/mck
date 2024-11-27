/* ==========================================================================
 * Authentication Controller Module
 *
 * PURPOSE: Manages user authentication flows and session handling
 * DEPENDENCIES: Spring MVC, Spring Security
 * SCOPE: Web authentication endpoints
 * 
 * SECURITY CONSIDERATIONS:
 * - CSRF protection enabled by default
 * - Session fixation protection
 * - Secure session cookie handling
 * ========================================================================== */

package com.demoproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

/* --------------------------------------------------------------------------
 * Authentication Controller Implementation
 * 
 * FUNCTIONALITY:
 * - Handles login page display
 * - Manages logout status
 * - Provides view resolution
 * 
 * IMPORTANT NOTES:
 * - Relies on Spring Security for auth processing
 * - Uses Thymeleaf template resolution
 * - Stateless design pattern
 * -------------------------------------------------------------------------- */
@Controller
public class AuthController {

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
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }
}