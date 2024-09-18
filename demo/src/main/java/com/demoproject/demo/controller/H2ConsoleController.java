package com.demoproject.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller class for handling access to the H2 database console.
 * This controller provides a secure way to access the H2 console
 * by restricting access to users with the ADMIN role.
 */
@Controller
public class H2ConsoleController {

    /**
     * Handles the request to access the H2 console.
     * This method is only accessible to users with the ADMIN role.
     * 
     * @return A string that redirects to the H2 console URL.
     */
    @GetMapping("/h2-console-access")
    @PreAuthorize("hasRole('ADMIN')")
    public String accessH2Console() {
        return "redirect:/h2-console";
    }
}