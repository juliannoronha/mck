package com.demoproject.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demoproject.demo.entity.Pac;

@Repository
public interface PacRepository extends JpaRepository<Pac, Long> {
    List<Pac> findByUser_Username(String username);

    @Query(value = "SELECT u.username, " +
           "COUNT(p.id) as totalSubmissions, " +
           "AVG(EXTRACT(EPOCH FROM (p.end_time - p.start_time))) / 3600 as avgTimeDuration, " +
           "AVG(CAST(p.pouches_checked AS DOUBLE PRECISION) / (EXTRACT(EPOCH FROM (p.end_time - p.start_time)) / 3600)) as avgPouchesPerHour, " +
           "SUM(p.pouches_checked) as totalPouchesChecked " +
           "FROM pac p " +
           "JOIN users u ON p.user_id = u.id " +
           "GROUP BY u.username",
           countQuery = "SELECT COUNT(DISTINCT u.username) FROM pac p JOIN users u ON p.user_id = u.id",
           nativeQuery = true)
    Page<Object[]> getUserProductivityDataPaginated(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pac p WHERE " +
           "(:name IS NULL OR LOWER(p.user.username) LIKE %:name%) AND " +
           "(:store IS NULL OR p.store = :store) AND " +
           "(:month IS NULL OR MONTH(p.startTime) = :month)")
    long countAllWithFilters(@Param("name") String name, 
                             @Param("store") String store, 
                             @Param("month") Integer month);

    @Query("SELECT p FROM Pac p JOIN FETCH p.user u WHERE " +
           "(:nameFilter IS NULL OR LOWER(u.username) LIKE %:nameFilter%) AND " +
           "(:store IS NULL OR p.store = :store) AND " +
           "(:month IS NULL OR MONTH(p.submissionDate) = :month)")
    Page<Pac> findAllWithFilters(Pageable pageable, 
                                 @Param("nameFilter") String nameFilter, 
                                 @Param("store") String store, 
                                 @Param("month") Integer month);

    @Query("SELECT p.user.username, " +
           "COUNT(p), " +
           "COALESCE(SUM(p.pouchesChecked), 0), " +
           "COALESCE(SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, p.startTime, p.endTime)), 0), " +
           "CASE WHEN SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, p.startTime, p.endTime)) > 0 " +
           "     THEN CAST(SUM(p.pouchesChecked) AS double) / (SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, p.startTime, p.endTime)) / 60.0) " +
           "     ELSE 0.0 END " +
           "FROM Pac p " +
           "GROUP BY p.user.username")
    List<Object[]> getUserProductivityData();
}