package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.dto.UserProductivityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * Repository interface for managing UserAnswer entities.
 * Extends JpaRepository to inherit basic CRUD operations.
 */
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    List<UserAnswer> findByName(String username);

    @Query(value = "SELECT u.username, " +
           "COUNT(ua.id) as totalSubmissions, " +
           "TO_CHAR(AVG(EXTRACT(EPOCH FROM (ua.end_time - ua.start_time))) / 3600, 'FM999990.00') as avgTimeDuration, " +
           "AVG(CAST(ua.pouches_checked AS DOUBLE PRECISION) / (EXTRACT(EPOCH FROM (ua.end_time - ua.start_time)) / 3600)) as avgPouchesPerHour, " +
           "SUM(ua.pouches_checked) as totalPouchesChecked, " +
           "AVG(CAST(ua.pouches_checked AS DOUBLE PRECISION)) as avgPouchesChecked " +
           "FROM user_answer ua " +
           "JOIN users u ON ua.user_id = u.id " +
           "GROUP BY u.username", nativeQuery = true)
    List<Object[]> getUserProductivityData();
}