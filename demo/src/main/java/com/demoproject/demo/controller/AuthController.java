package com.demoproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

/**
 * Controller responsible for authentication operations.
 * Handles login and logout functionality.
 */
@Controller
public class AuthController {

    /**
     * Displays the login page.
     * @param logout Indicates if the user has logged out
     * @param model The Model object to add attributes
     * @return The name of the login view
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }
}