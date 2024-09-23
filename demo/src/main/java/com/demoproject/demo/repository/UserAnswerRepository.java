package com.demoproject.demo.repository;

import com.demoproject.demo.dto.UserProductivityDTO;
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
    
    @Query("SELECT new com.demoproject.demo.dto.UserProductivityDTO(" +
           "u.name, " +
           "COUNT(u), " +
           "FUNCTION('TIME_FORMAT', SEC_TO_TIME(AVG(FUNCTION('TIME_TO_SEC', TIMEDIFF(u.endTime, u.startTime)))), '%H:%i:%s'), " +
           "CAST(SUM(u.pouchesChecked) AS double) / (CAST(SUM(FUNCTION('TIME_TO_SEC', TIMEDIFF(u.endTime, u.startTime))) AS double) / 3600.0), " +
           "SUM(u.pouchesChecked), " +
           "CAST(AVG(CAST(u.pouchesChecked AS double)) AS double)) " +
           "FROM UserAnswer u " +
           "GROUP BY u.name")
    List<UserProductivityDTO> getUserProductivityData();
    
    @Query(value = "SELECT u.name as username, " +
           "COUNT(*) as totalSubmissions, " +
           "SUM(u.pouches_checked) as totalPouchesChecked, " +
           "SUM(EXTRACT(EPOCH FROM (u.end_time - u.start_time)) / 60) as totalMinutes " +
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
    
    @Query(value = "EXPLAIN ANALYZE " +
           "SELECT ua.name, COUNT(ua), AVG(EXTRACT(EPOCH FROM (ua.end_time - ua.start_time)) / 60), " +
           "AVG(ua.pouches_checked / (EXTRACT(EPOCH FROM (ua.end_time - ua.start_time)) / 3600)) " +
           "FROM user_answer ua GROUP BY ua.name", nativeQuery = true)
    List<String> explainGetUserProductivityData();
}