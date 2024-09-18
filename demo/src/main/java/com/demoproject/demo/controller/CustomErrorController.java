package com.demoproject.demo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller to handle and display error pages.
 * This controller implements ErrorController to override the default error handling behavior.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles error requests and determines the appropriate error message to display.
     *
     * @param request The HttpServletRequest containing error attributes
     * @param model The Model to add attributes for the view
     * @return The name of the error view to be rendered
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "Error, you cannot access this page!";
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMessage = "Sorry, the page you are looking for could not be found.";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMessage = "Sorry, you don't have permission to access this page.";
            }
        }
        
        // Add the error message to the model for display in the view
        model.addAttribute("errorMessage", errorMessage);
        return "error"; // Return the name of the error view template
    }
}