package com.demoproject.demo.services;

import com.demoproject.demo.entity.Role;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.RoleRepository;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.userdto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String PROTECTED_USERNAME = "jn";

    /**
     * Constructor for UserService.
     * @param userRepository Repository for user data operations.
     * @param roleRepository Repository for role data operations.
     * @param passwordEncoder Encoder for password hashing.
     */
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new role.
     * @param roleName The name of the role to create.
     * @return The created role.
     * @throws DataIntegrityViolationException if the role already exists.
     */
    public Role createRole(String roleName) {
        Role existingRole = roleRepository.findByName(roleName.toUpperCase());
        if (existingRole != null) {
            throw new DataIntegrityViolationException("Role already exists");
        }
        Role newRole = new Role(roleName.toUpperCase());
        return roleRepository.save(newRole);
    }

    /**
     * Registers a new user.
     * @param userDTO Data Transfer Object containing user information.
     * @throws DataIntegrityViolationException if the username already exists.
     */
    public void registerNewUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new DataIntegrityViolationException("Username already exists");
        }
        Role role = roleRepository.findByName(userDTO.getRole());
        if (role == null) {
            throw new IllegalArgumentException("Invalid role");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(role);
        userRepository.save(user);
        logger.info("New user registered: {}", userDTO.getUsername());
    }

    /**
     * Deletes a user by username.
     * @param username The username of the user to delete.
     * @throws IllegalArgumentException if attempting to delete the protected user.
     * @throws RuntimeException if the user is not found.
     */
    public void deleteUser(String username) {
        userRepository.findByUsername(username)
            .ifPresentOrElse(
                user -> {
                    if (PROTECTED_USERNAME.equals(username)) {
                        throw new IllegalArgumentException("Cannot delete protected user");
                    }
                    userRepository.delete(user);
                },
                () -> {
                    throw new UsernameNotFoundException("User not found: " + username);
                }
            );
    }

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
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