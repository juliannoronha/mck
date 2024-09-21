package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing UserAnswer entities.
 * Extends JpaRepository to inherit basic CRUD operations.
 */
@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    /**
     * Deletes a UserAnswer entity by its ID.
     * 
     * @param id The ID of the UserAnswer to be deleted.
     */
    void deleteById(Long id);
    
    List<UserAnswer> findByName(String name);
    
    @Query("SELECT ua.name, COUNT(ua), AVG(TIMESTAMPDIFF(MINUTE, ua.startTime, ua.endTime)), " +
           "AVG(ua.pouchesChecked / (TIMESTAMPDIFF(HOUR, ua.startTime, ua.endTime))) " +
           "FROM UserAnswer ua GROUP BY ua.name")
    List<Object[]> getUserProductivityData();
}