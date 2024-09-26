package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    List<UserAnswer> findByUser_Username(String username);

    @Query(value = "SELECT u.username, " +
           "COUNT(ua.id) as totalSubmissions, " +
           "TO_CHAR(AVG(EXTRACT(EPOCH FROM (p.end_time - p.start_time))) / 3600, 'FM999990.00') as avgTimeDuration, " +
           "COALESCE(AVG(CAST(p.pouches_checked AS DOUBLE PRECISION) / NULLIF(EXTRACT(EPOCH FROM (p.end_time - p.start_time)) / 3600, 0)), 0) as avgPouchesPerHour, " +
           "COALESCE(SUM(p.pouches_checked), 0) as totalPouchesChecked " +
           "FROM user_answer ua " +
           "JOIN users u ON ua.user_id = u.id " +
           "JOIN pac p ON ua.id = p.user_answer_id " +
           "GROUP BY u.username", nativeQuery = true)
    List<Object[]> getUserProductivityData();

    @Query(value = "SELECT u.username, " +
           "COUNT(ua.id) as totalSubmissions, " +
           "TO_CHAR(AVG(EXTRACT(EPOCH FROM (p.end_time - p.start_time))) / 3600, 'FM999990.00') as avgTimeDuration, " +
           "AVG(CAST(p.pouches_checked AS DOUBLE PRECISION) / (EXTRACT(EPOCH FROM (p.end_time - p.start_time)) / 3600)) as avgPouchesPerHour, " +
           "SUM(p.pouches_checked) as totalPouchesChecked " +
           "FROM user_answer ua " +
           "JOIN users u ON ua.user_id = u.id " +
           "JOIN pac p ON ua.id = p.user_answer_id " +
           "GROUP BY u.username",
           countQuery = "SELECT COUNT(DISTINCT u.username) FROM user_answer ua JOIN users u ON ua.user_id = u.id",
           nativeQuery = true)
    Page<Object[]> getUserProductivityDataPaginated(Pageable pageable);

    @Query("SELECT ua FROM UserAnswer ua LEFT JOIN FETCH ua.pac LEFT JOIN FETCH ua.user")
    Page<UserAnswer> findAllWithPac(Pageable pageable);
}