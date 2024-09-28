package com.demoproject.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.demoproject.demo.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations for User objects,
 * as well as custom query methods for specific user-related operations.
 *
 * @see User
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their username.
     * 
     * This method performs a case-sensitive search for the exact username match.
     * 
     * @param username The username to search for (case-sensitive)
     * @return An Optional containing the User if found, or an empty Optional if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Retrieves all users who have submitted at least one UserAnswer.
     * 
     * This query uses a DISTINCT clause to ensure each user is returned only once,
     * regardless of how many submissions they have made.
     *
     * @return A List of User objects who have made submissions
     */
    @Query("SELECT DISTINCT ua.user FROM UserAnswer ua")
    List<User> findUsersWithSubmissions();

    // TODO: Implement a method to find users by role
    // TODO: Add a query to retrieve users who haven't logged in for a specific period
}