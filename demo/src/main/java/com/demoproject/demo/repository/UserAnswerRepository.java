package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
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
    
    @Query(value = "SELECT u.name as username, " +
           "COUNT(*) as totalSubmissions, " +
           "SUM(u.pouches_checked) as totalPouchesChecked, " +
           "SUM(TIMESTAMPDIFF(MINUTE, u.start_time, u.end_time)) as totalMinutes " +
           "FROM user_answer u GROUP BY u.name",
           nativeQuery = true)
    List<UserProductivityQueryProjection> getUserProductivitySummary(Pageable pageable);

    interface UserProductivityQueryProjection {
        String getUsername();
        Long getTotalSubmissions();
        Long getTotalPouchesChecked();
        Long getTotalMinutes();
    }
    
    @Query("SELECT SUM(ua.pouchesChecked) FROM UserAnswer ua WHERE ua.name = :username")
    Long getTotalPouchesCheckedByUser(@Param("username") String username);

    @Query("SELECT SUM(ua.pouchesChecked) FROM UserAnswer ua")
    Long getTotalPouchesChecked();
}