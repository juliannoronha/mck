package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.services.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles the root URL request.
     * @return Redirects to the home page
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";  // Redirect to home instead of login
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
        logger.info("Received registration request for user: {} with role: {}", user.getUsername(), user.getRole());
        
        if (result.hasErrors()) {
            logger.warn("Validation errors in registration form");
            return "register";
        }
        
        try {
            userService.registerNewUser(user);
            logger.info("User registered successfully: {} with role: {}", user.getUsername(), user.getRole());
            redirectAttributes.addFlashAttribute("successMessage", "User registered successfully!");
            return "redirect:/users";
        } catch (DataIntegrityViolationException e) {
            logger.error("Username already exists: {}", user.getUsername());
            result.rejectValue("username", "error.user", "Username already exists");
            return "register";
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role selected: {}", user.getRole());
            result.rejectValue("role", "error.user", e.getMessage());
            return "register";
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
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
    public ResponseEntity<?> deleteUser(@RequestParam String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/packmed")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN', 'MODERATOR')")
    public String packmed(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessed packmed page", username);
        model.addAttribute("username", username);
        return "packmed";
    }

}