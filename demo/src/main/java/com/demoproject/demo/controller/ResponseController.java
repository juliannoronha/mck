package com.demoproject.demo.controller;

import com.demoproject.demo.entity.UserAnswer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;

import com.demoproject.demo.annotation.RequiresAuthentication;

import com.demoproject.demo.services.ResponseService;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Controller class for handling various HTTP requests related to user management,
 * authentication, and user responses.
 */
@Controller
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    /**
     * Displays the questions page.
     * @return The name of the packmed view
     */
    @GetMapping("/questions")
    @RequiresAuthentication
    public String showQuestions() {
        return "packmed";
    }

    /**
     * Handles submission of user answers.
     * @param userAnswer The UserAnswer object containing the user's responses
     * @param authentication Authentication object for checking access
     * @return Redirects to the packmed page
     */
    @PostMapping("/submit-questions")
    @RequiresAuthentication
    @ResponseBody
    public ResponseEntity<String> submitQuestions(@ModelAttribute UserAnswer userAnswer, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sorry, you don't have access to that!");
        }
        try {
            String username = authentication.getName();
            responseService.submitUserAnswer(userAnswer, username);
            return ResponseEntity.ok("You have successfully submitted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Displays all user responses.
     * @param model The Model object to add attributes
     * @return The name of the responses view
     */
    @GetMapping("/view-responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String viewResponses(Model model, 
                                @RequestParam(defaultValue = "0") int page, 
                                @RequestParam(defaultValue = "10") int size) {
        Page<UserAnswer> responses = responseService.getAllResponses(PageRequest.of(page, size));
        model.addAttribute("responses", responses);
        return "responses";  // This will render the responses.html template
    }

    /**
     * Displays the packmed page.
     * @return The name of the packmed view
     */
    @GetMapping("/packmed")
    public String packmed() {
        return "packmed";
    }

    /**
     * Handles deletion of a user response.
     * @param id The ID of the response to be deleted
     * @param redirectAttributes RedirectAttributes for adding flash attributes
     * @return Redirects to the view-responses page
     */
    @PostMapping("/delete-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String deleteResponse(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            responseService.deleteResponse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Response deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting response: " + e.getMessage());
        }
        return "redirect:/view-responses";
    }
}