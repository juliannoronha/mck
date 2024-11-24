package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service class for managing user password operations.
 * This class provides functionality for changing user passwords securely.
 */
@Service
public class PasswordManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new PasswordManagementService with necessary dependencies.
     * 
     * @param userRepository Repository for user data operations
     * @param passwordEncoder Encoder for secure password hashing
     */
    public PasswordManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Changes the password for a given user.
     * 
     * @param username The username of the user whose password is to be changed
     * @param newPassword The new password to set
     * @throws RuntimeException if the user is not found
     * @throws IllegalArgumentException if the new password is empty or null
     */
    @Transactional
    public void changePassword(String username, String newPassword) {
        // Retrieve user or throw exception if not found
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        // TODO: Implement additional password validation rules
        // Consider checking password strength, length, complexity, etc.

        // Encode and set the new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // Persist the updated user information
        userRepository.save(user);
    }

    // TODO: Implement additional password-related methods
    // Examples:
    // - Password strength checker
    // - Password reset functionality
    // - Password expiration management
}
