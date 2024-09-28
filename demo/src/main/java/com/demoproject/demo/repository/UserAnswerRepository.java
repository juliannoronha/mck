package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing UserAnswer entities.
 * Provides methods for querying and manipulating UserAnswer data in the database.
 */
@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    /**
     * Finds all UserAnswer entries for a given username.
     * @param username The username to search for
     * @return A list of UserAnswer entries associated with the given username
     */
    List<UserAnswer> findByUser_Username(String username);

    /**
     * Retrieves all UserAnswer entries for a specific username using JPQL.
     * @param username The username to search for
     * @return A list of UserAnswer entries for the given username
     */
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.user.username = :username")
    List<UserAnswer> findAllByUsername(@Param("username") String username);

    /**
     * Counts the number of UserAnswer entries for a specific username.
     * @param username The username to count answers for
     * @return The number of UserAnswer entries for the given username
     */
    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.username = :username")
    long countByUsername(@Param("username") String username);

    /**
     * Retrieves all UserAnswer entries without any specific criteria.
     * @return A list of all UserAnswer entries in the database
     */
    @Query("SELECT ua FROM UserAnswer ua")
    List<UserAnswer> findAllReadOnly();

    // TODO: Consider adding methods for filtering UserAnswers by date range
    // TODO: Implement a method to find UserAnswers by specific criteria (e.g., submission date, answer content)
}
