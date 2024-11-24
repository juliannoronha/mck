package com.demoproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsible for handling page navigation and routing.
 */
@Controller
public class PageController {
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    /**
     * Handles the root URL request.
     * @return Redirects to the home page
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Displays the home page for authenticated users.
     * @param model The Model object to add attributes
     * @return The name of the home view or redirects to login if not authenticated
     */
    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }
        model.addAttribute("username", auth.getName());
        return "home";
    }

    /**
     * Displays the packmed page for authorized users.
     * @param model The Model object to add attributes
     * @param authentication The Authentication object for the current user
     * @return The name of the packmed view
     */
    @GetMapping("/packmed")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN', 'MODERATOR')")
    public String packmed(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessed packmed page", username);
        model.addAttribute("username", username);
        return "packmed";
    }

    /**
     * Displays the wellca page for authorized users.
     * @param model The Model object to add attributes
     * @param authentication The Authentication object for the current user
     * @return The name of the wellca view
     */
    @GetMapping("/wellca")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN', 'MODERATOR')")
    public String wellca(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessed wellca page", username);
        model.addAttribute("username", username);
        return "wellca";
    }

    /**
     * Displays the NBA stats page for authorized users.
     * @param model The Model object to add attributes
     * @param authentication The Authentication object for the current user
     * @return The name of the nba view
     */
    @GetMapping("/nba")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public String nba(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessed NBA stats page", username);
        model.addAttribute("username", username);
        return "nba";
    }
}