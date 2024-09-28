package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.User;
import java.util.Optional;

/**
 * Service responsible for handling user deletion operations.
 * This class provides functionality to safely delete users from the system.
 */
@Service
public class UserDeletionService {
    private final UserRepository userRepository;

    /**
     * Constructs a new UserDeletionService with the necessary repository.
     * 
     * @param userRepository The repository used for user data operations
     */
    public UserDeletionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Deletes a user from the system based on their username.
     * This method ensures that admin users cannot be deleted.
     * 
     * @param username The username of the user to be deleted
     * @throws IllegalStateException if the user is an admin
     * @throws IllegalArgumentException if the user is not found
     */
    @Transactional
    public void deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Prevent deletion of admin users
            if ("ADMIN".equals(user.getRole())) {
                throw new IllegalStateException("Admin users cannot be deleted");
            }
            
            // Perform the actual user deletion
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException("User not found: " + username);
        }
    }

    // TODO: Implement a method to handle cascading deletions (e.g., user's posts, comments)
    // TODO: Add an audit log for user deletions
}