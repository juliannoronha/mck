package com.demoproject.demo.controller;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;

import org.springframework.ui.Model;

import com.demoproject.demo.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import com.demoproject.demo.annotation.RequiresAuthentication;

/**
 * Controller class for handling various HTTP requests related to user management,
 * authentication, and user responses.
 */
@Controller
public class ResponseController {

    private final UserAnswerRepository userAnswerRepository;

    /**
     * Constructor for HelloController.
     * @param userAnswerRepository Repository for managing UserAnswer entities
     * @param userService Service for managing User-related operations
     */
    public ResponseController(UserAnswerRepository userAnswerRepository, UserService userService) {
        this.userAnswerRepository = userAnswerRepository;
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
     * @param redirectAttributes RedirectAttributes for adding flash attributes
     * @return Redirects to the packmed page
     */
    @PostMapping("/submit-questions")
    @RequiresAuthentication
    public String submitQuestions(@ModelAttribute UserAnswer userAnswer, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        userAnswer.setName(username);
        userAnswer.setSubmissionDate(LocalDate.now()); // Set the submission date
        userAnswerRepository.save(userAnswer);
        
        redirectAttributes.addFlashAttribute("successMessage", "You have successfully submitted!");
        return "redirect:/packmed";
    }

    /**
     * Displays all user responses.
     * @param model The Model object to add attributes
     * @return The name of the responses view
     */
    @GetMapping("/view-responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // Change this line
    public String viewResponses(Model model) {
        List<UserAnswer> responses = userAnswerRepository.findAll();
        model.addAttribute("responses", responses);
        return "responses";
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")  // Add this line
    public String deleteResponse(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        userAnswerRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Response deleted successfully!");
        return "redirect:/view-responses";
    }
}