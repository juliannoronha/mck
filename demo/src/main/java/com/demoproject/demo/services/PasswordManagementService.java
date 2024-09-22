package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class PasswordManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        // You might want to add more password validation rules here
        // For example, check password strength, length, etc.

        // Encode and set the new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // Save the updated user
        userRepository.save(user);
    }

    // You might want to add more password-related methods here
    // For example, a method to check password strength, reset password, etc.
}
