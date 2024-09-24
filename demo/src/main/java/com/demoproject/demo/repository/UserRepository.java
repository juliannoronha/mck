package com.demoproject.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.demoproject.demo.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * Extends JpaRepository to provide CRUD operations for User objects.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by their username.
     * 
     * @param username The username to search for
     * @return An Optional containing the User if found, or an empty Optional if not found
     */
    Optional<User> findByUsername(String username);
    
    @Query("SELECT DISTINCT ua.user FROM UserAnswer ua")
    List<User> findUsersWithSubmissions();
}