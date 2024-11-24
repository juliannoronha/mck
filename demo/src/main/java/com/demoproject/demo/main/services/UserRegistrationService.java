package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserRepository;

/**
 * Service responsible for user registration and related operations.
 * This class handles the creation of new user accounts, input validation,
 * and role conversion.
 */
@Service
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserRegistrationService with necessary dependencies.
     *
     * @param userRepository Repository for user data operations
     * @param passwordEncoder Encoder for securing user passwords
     */
    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     *
     * @param userDTO Data Transfer Object containing user registration information
     * @return The newly created User entity
     * @throws IllegalArgumentException if the username already exists or input is invalid
     */
    @Transactional
    public User registerNewUser(UserDTO userDTO) {
        // Check for existing username to prevent duplicates
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        validateUserInput(userDTO);

        // Create and populate new user entity
        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setRole(convertStringToRole(userDTO.getRole()));

        // Persist the new user and return the saved entity
        return userRepository.save(newUser);
    }

    /**
     * Validates user input for registration.
     *
     * @param userDTO User data to validate
     * @throws IllegalArgumentException if any required field is empty or null
     */
    private void validateUserInput(UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (userDTO.getRole() == null || userDTO.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        // TODO: Implement additional validation (e.g., password strength, email format)
    }

    /**
     * Converts a string role to the corresponding User.Role enum.
     *
     * @param roleString String representation of the role
     * @return Corresponding User.Role enum value
     * @throws IllegalArgumentException if the role string is invalid
     */
    private User.Role convertStringToRole(String roleString) {
        try {
            return User.Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleString);
        }
    }
}
