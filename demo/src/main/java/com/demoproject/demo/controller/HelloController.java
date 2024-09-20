package com.demoproject.demo.controller;

import com.demoproject.demo.userdto.UserDTO;
import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;

import org.springframework.ui.Model;

import com.demoproject.demo.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controller class for handling various HTTP requests related to user management,
 * authentication, and user responses.
 */
@Controller
public class HelloController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
    private final UserAnswerRepository userAnswerRepository;

    /**
     * Constructor for HelloController.
     * @param userAnswerRepository Repository for managing UserAnswer entities
     * @param userService Service for managing User-related operations
     */
    public HelloController(UserAnswerRepository userAnswerRepository, UserService userService) {
        this.userAnswerRepository = userAnswerRepository;
        this.userService = userService;
    }

    /**
     * Handles the root URL request.
     * @return Redirects to the login page
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    /**
     * Handles the request for the home page.
     * @return The name of the home view
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
     * Handles the request for the login page.
     * @return The name of the login view
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "login";
    }

    /**
     * Displays the registration form.
     * @param model The Model object to add attributes
     * @return The name of the register view
     */
    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return "register";
    }

    /**
     * Handles user registration.
     * @param user The UserDTO object containing user information
     * @param result BindingResult for validation errors
     * @param model The Model object to add attributes
     * @param redirectAttributes RedirectAttributes for adding flash attributes
     * @return Redirects to the users page if successful, otherwise returns the register view
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerUser(@ModelAttribute("user") @Valid UserDTO user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User registered successfully!");
            return "redirect:/users";
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("username", "error.user", "Username already exists");
            return "register";
        } catch (IllegalArgumentException e) {
            result.rejectValue("role", "error.user", "Invalid role selected");
            return "register";
        }
    }

    /**
     * Displays the list of users.
     * @param model The Model object to add attributes
     * @return The name of the users view
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    /**
     * Handles user deletion.
     * @param username The username of the user to be deleted
     * @return ResponseEntity with appropriate status and message
     */
    @PostMapping("/users/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@RequestParam String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Attempt to delete protected user: {}", username);
            return ResponseEntity.badRequest().body("Cannot delete protected user");
        } catch (Exception e) {
            logger.error("Error deleting user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }

    /**
     * Displays the questions page.
     * @return The name of the packmed view
     */
    @GetMapping("/questions")
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
    public String submitQuestions(@ModelAttribute UserAnswer userAnswer, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        userAnswer.setName(username);
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
    @PreAuthorize("hasRole('ADMIN')")
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
    public String deleteResponse(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        userAnswerRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Response deleted successfully!");
        return "redirect:/view-responses";
    }
}