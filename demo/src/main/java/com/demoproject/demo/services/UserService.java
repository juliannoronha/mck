package com.demoproject.demo.services;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import com.demoproject.demo.dto.UserProductivityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.demoproject.demo.entity.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user-related operations.
 * This class acts as a facade, delegating specific operations to specialized services.
 * It provides a centralized point for user management, including registration, deletion,
 * productivity tracking, and password management.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Dependency injection of specialized services
    private final UserRegistrationService registrationService;
    private final UserDeletionService deletionService;
    private final UserProductivityService productivityService;
    private final PasswordManagementService passwordService;
    private final UserRepository userRepository;

    /**
     * Constructs a new UserService with all required dependencies.
     * 
     * @param registrationService Service for user registration
     * @param deletionService Service for user deletion
     * @param productivityService Service for tracking user productivity
     * @param passwordService Service for password management
     * @param userRepository Repository for user data access
     */
    public UserService(UserRegistrationService registrationService,
                       UserDeletionService deletionService,
                       UserProductivityService productivityService,
                       PasswordManagementService passwordService,
                       UserRepository userRepository) {
        this.registrationService = registrationService;
        this.deletionService = deletionService;
        this.productivityService = productivityService;
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    // User Management Operations

    /**
     * Registers a new user in the system.
     *
     * @param userDTO Data Transfer Object containing user registration information
     */
    public void registerNewUser(UserDTO userDTO) {
        logger.info("Registering new user: {}", userDTO.getUsername());
        registrationService.registerNewUser(userDTO);
    }

    /**
     * Deletes a user from the system by their username.
     *
     * @param username The username of the user to be deleted
     * @throws RuntimeException if the user is not found
     */
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    // User Productivity Operations

    /**
     * Retrieves productivity data for a specific user.
     *
     * @param username The username of the user
     * @return A map containing productivity metrics for the user
     */
    public Map<String, Object> getUserProductivity(String username) {
        logger.info("Retrieving productivity data for user: {}", username);
        return productivityService.getUserProductivity(username);
    }

    /**
     * Retrieves productivity data for all users.
     *
     * @return A list of UserProductivityDTO objects containing productivity data for all users
     */
    public List<UserProductivityDTO> getAllUserProductivity() {
        logger.info("Retrieving productivity data for all users.");
        return productivityService.getAllUserProductivity(0, Integer.MAX_VALUE).getContent();
    }

    /**
     * Retrieves paginated productivity data for all users.
     *
     * @param page The page number (zero-based)
     * @param size The size of each page
     * @return A Page of UserProductivityDTO objects
     */
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Retrieving productivity data for all users. Page: {}, Size: {}", page, size);
        return productivityService.getAllUserProductivity(page, size);
    }

    // Password Management

    /**
     * Changes the password for a specified user.
     *
     * @param username The username of the user
     * @param newPassword The new password to set
     */
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for user: {}", username);
        passwordService.changePassword(username, newPassword);
    }

    // User Retrieval

    /**
     * Retrieves all users with pagination support.
     *
     * @param pageable Pagination information
     * @return A Page of User objects
     */
    public Page<User> getAllUsers(Pageable pageable) {
        logger.info("Retrieving all users. Pageable: {}", pageable);
        return userRepository.findAll(pageable);
    }

    // TODO: Implement user role management functionality
    // TODO: Add method for updating user profile information
    // TODO: Consider adding caching for frequently accessed user data
}