package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.services.UserDeletionService;
import com.demoproject.demo.services.UserRegistrationService;
import com.demoproject.demo.services.UserService;
import com.demoproject.demo.services.PasswordManagementService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsible for user management operations.
 * Handles user registration, listing, deletion, and password management.
 */
@Controller
@RequestMapping("/users")
public class UserManagementController {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    private final UserService userService;
    private final PasswordManagementService passwordService;
    private final UserRegistrationService registrationService;
    private final UserDeletionService deletionService;

    public UserManagementController(UserService userService,
                                  PasswordManagementService passwordService,
                                  UserRegistrationService registrationService,
                                  UserDeletionService deletionService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.registrationService = registrationService;
        this.deletionService = deletionService;
    }

    /**
     * Displays the registration form.
     */
    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        logger.info("Showing registration form");
        return "register";
    }

    /**
     * Handles user registration process.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerUser(@ModelAttribute("user") @Valid UserDTO user, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.warn("Validation errors in registration form");
            return "register";
        }
        
        try {
            registrationService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User registered successfully!");
            return "redirect:/users";
        } catch (Exception e) {
            logger.error("Error registering new user", e);
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }

    /**
     * Displays a paginated list of users for admin users.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model, 
                          @RequestParam(defaultValue = "0") int page, 
                          @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getAllUsers(PageRequest.of(page, size));
        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        return "users";
    }

    /**
     * Handles user deletion for admin users.
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@RequestParam String username) {
        try {
            deletionService.deleteUser(username);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Handles password change requests.
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestParam String username, 
                                          @RequestParam String newPassword, 
                                          Authentication authentication) {
        if (!authentication.getName().equals(username) && 
            !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("You don't have permission to change this password");
        }
        try {
            passwordService.changePassword(username, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}