package com.demoproject.demo.services;

import com.demoproject.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.demoproject.demo.entity.User;

/**
 * Service class for managing user-related operations.
 * This class acts as a facade for basic user management operations.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    /**
     * Constructs a new UserService with required dependencies.
     * 
     * @param userRepository Repository for user data access
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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