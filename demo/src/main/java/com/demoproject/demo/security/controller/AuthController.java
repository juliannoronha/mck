package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.services.PasswordManagementService;
import com.demoproject.demo.services.UserRegistrationService;
import com.demoproject.demo.services.UserService;
import com.demoproject.demo.services.UserDeletionService;

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
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthController handles authentication-related operations and user management.
 * This controller manages user registration, login, and various user-related actions.
 */
@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordManagementService passwordService;
    private final UserRegistrationService registrationService;
    private final UserDeletionService deletionService;

    public AuthController(UserService userService, 
                         PasswordManagementService passwordService,
                         UserRegistrationService registrationService,
                         UserDeletionService deletionService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.registrationService = registrationService;
        this.deletionService = deletionService;
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

    /**
     * Displays the registration form for admin users.
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
     * Handles user registration process.
     * @param user The UserDTO object containing user information
     * @param result BindingResult for validation errors
     * @param redirectAttributes RedirectAttributes for adding flash attributes
     * @return Redirects to the users page if successful, otherwise returns to the register view
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public String registerUser(@ModelAttribute("user") @Valid UserDTO user, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            registrationService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User registered successfully!");
            return "redirect:/users";
        } catch (Exception e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }

    /**
     * Displays a paginated list of users for admin users.
     * @param model The Model object to add attributes
     * @param page The page number (default 0)
     * @param size The page size (default 10)
     * @return The name of the users view
     */
    @GetMapping("/users")
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
     * @param username The username of the user to be deleted
     * @return ResponseEntity with appropriate status and message
     */
    @PostMapping("/users/delete")
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
     * Handles password change requests.
     * @param username The username of the user whose password is being changed
     * @param newPassword The new password
     * @param authentication The Authentication object for the current user
     * @return ResponseEntity with appropriate status and message
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

    @GetMapping("/wellca")
    @PreAuthorize("hasAnyRole('CHECKER', 'ADMIN', 'MODERATOR')")
    public String wellca(Model model, Authentication authentication) {
        String username = authentication.getName();
        logger.info("User {} accessed wellca page", username);
        model.addAttribute("username", username);
        return "wellca";
    }
}