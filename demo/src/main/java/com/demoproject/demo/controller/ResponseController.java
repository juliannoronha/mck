package com.demoproject.demo.controller;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.entity.Pac;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.demoproject.demo.annotation.RequiresAuthentication;
import com.demoproject.demo.services.ResponseService;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping("/questions")
    @RequiresAuthentication
    public String showQuestions() {
        return "packmed";
    }

    @PostMapping("/submit-questions")
    @RequiresAuthentication
    @ResponseBody
    public ResponseEntity<String> submitQuestions(@ModelAttribute UserAnswer userAnswer, @ModelAttribute Pac pac, BindingResult bindingResult, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        try {
            // Set the storeId for the Pac entity
            pac.setStoreId(userAnswer.getStore());
            responseService.submitUserAnswer(userAnswer, pac, authentication.getName());
            return ResponseEntity.ok("Response submitted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting questions: " + e.getMessage());
        }
    }

    @PostMapping("/delete-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<String> deleteResponse(@RequestParam Long id) {
        try {
            responseService.deleteResponse(id);
            return ResponseEntity.ok("Response deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace to the console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting response: " + e.getMessage() + ". Check server logs for details.");
        }
    }

    @GetMapping("/view-responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String viewResponses(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<UserAnswer> responses = responseService.getAllResponsesWithPac(PageRequest.of(page, size));
        model.addAttribute("responses", responses);
        return "responses";
    }
}