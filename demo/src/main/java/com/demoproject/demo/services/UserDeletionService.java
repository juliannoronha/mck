/* =============================================================================
 * User Deletion Service
 * =============================================================================
 * PURPOSE: Manages secure deletion of user accounts from the system
 * 
 * CORE FUNCTIONALITY:
 * - Safe user deletion with role validation
 * - Transactional integrity
 * - Admin protection
 * 
 * DEPENDENCIES:
 * - Spring Framework (Service, Transactional)
 * - UserRepository for persistence
 * - User entity
 * 
 * SECURITY NOTES:
 * - Prevents deletion of admin accounts
 * - Validates user existence before deletion
 * ============================================================================= */
package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.User;
import java.util.Optional;

@Service
public class UserDeletionService {

    /* --------------------------------------------------------------------------
     * Service Dependencies
     * -------------------------------------------------------------------------- */
    private final UserRepository userRepository;

    /**
     * Initializes deletion service with required data access.
     * 
     * @param userRepository Data access for user operations
     * @note Repository must be non-null
     */
    public UserDeletionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* --------------------------------------------------------------------------
     * Deletion Operations
     * -------------------------------------------------------------------------- */

    /**
     * Executes user deletion with security validation.
     * 
     * @param username Target user identifier
     * @throws IllegalStateException Admin deletion attempted
     * @throws IllegalArgumentException User not found
     * @security Prevents admin deletion
     * @performance Single transaction scope
     * @note Consider audit logging before deletion
     */
    @Transactional
    public void deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Security: Block admin deletion
            if ("ADMIN".equals(user.getRole())) {
                throw new IllegalStateException("Admin users cannot be deleted");
            }
            
            // Execute deletion within transaction
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException("User not found: " + username);
        }
    }

    /* --------------------------------------------------------------------------
     * Future Enhancements
     * -------------------------------------------------------------------------- */

    /* @todo Implementation needs:
     * - Cascading deletion support
     * - Audit logging integration
     * - Soft deletion option
     * - Backup before deletion
     * - Rate limiting for bulk operations
     */
}