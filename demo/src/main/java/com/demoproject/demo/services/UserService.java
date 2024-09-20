package com.demoproject.demo.services;

import com.demoproject.demo.userdto.UserDTO;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constructor for UserService.
     * @param userRepository Repository for user data operations.
     * @param passwordEncoder Encoder for password hashing.
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * @param userDTO Data Transfer Object containing user information.
     * @throws DataIntegrityViolationException if the username already exists.
     */
    @Transactional
    public void registerNewUser(UserDTO userDTO) {
        logger.info("Attempting to register new user: {}", userDTO.getUsername());
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Username already exists: {}", userDTO.getUsername());
            throw new DataIntegrityViolationException("Username already exists");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(User.Role.valueOf(userDTO.getRole().name()));
        logger.info("Saving user to database: {}", user);
        User savedUser = userRepository.save(user);
        userRepository.flush();
        logger.info("User registered successfully: {}", savedUser);
    }

    /**
     * Deletes a user by username.
     * @param username The username of the user to delete.
     * @throws IllegalArgumentException if attempting to delete the protected user.
     * @throws RuntimeException if the user is not found.
     */
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is a protected user (e.g., admin)
        if ("admin".equals(username)) {
            throw new IllegalArgumentException("Cannot delete protected user");
        }
        
        userRepository.delete(user);
        logger.info("User deleted: {}", username);
    }

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users from the database", users.size());
        return users;
    }

    /**
     * Checks if a user exists by username.
     * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Deletes a user by username.
     * @param username The username of the user to delete.
     * @throws RuntimeException if the user is not found.
     */
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}