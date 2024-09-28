package com.demoproject.demo.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.demoproject.demo.entity.Pac;

/**
 * Repository interface for managing Pac (Pouch Accuracy Check) entities.
 * Provides methods for querying and manipulating Pac data in the database.
 */
@Repository
public interface PacRepository extends JpaRepository<Pac, Long> {

    /**
     * Finds all Pac entries for a given username.
     * @param username The username to search for
     * @return A list of Pac entries associated with the given username
     */
    List<Pac> findByUser_Username(String username);

    /**
     * Retrieves paginated user productivity data.
     * This query aggregates Pac data to provide insights on user performance.
     * @param pageable Pagination information
     * @return A page of Object arrays containing productivity metrics
     */
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

    /**
     * Counts Pac entries based on specified filters.
     * @param name Optional username filter (case-insensitive, partial match)
     * @param store Optional store filter (exact match)
     * @param month Optional month filter
     * @return The count of Pac entries matching the specified filters
     */
    @Query("SELECT COUNT(p) FROM Pac p WHERE " +
           "(:name IS NULL OR LOWER(p.user.username) LIKE %:name%) AND " +
           "(:store IS NULL OR p.store = :store) AND " +
           "(:month IS NULL OR MONTH(p.startTime) = :month)")
    long countAllWithFilters(@Param("name") String name, 
                             @Param("store") String store, 
                             @Param("month") Integer month);

    /**
     * Retrieves paginated Pac entries based on specified filters.
     * @param pageable Pagination information
     * @param nameFilter Optional username filter (case-insensitive, partial match)
     * @param store Optional store filter (exact match)
     * @param month Optional month filter
     * @return A page of Pac entries matching the specified filters
     */
    @Query("SELECT p FROM Pac p JOIN FETCH p.user u WHERE " +
           "(:nameFilter IS NULL OR LOWER(u.username) LIKE %:nameFilter%) AND " +
           "(:store IS NULL OR p.store = :store) AND " +
           "(:month IS NULL OR MONTH(p.submissionDate) = :month)")
    Page<Pac> findAllWithFilters(Pageable pageable, 
                                 @Param("nameFilter") String nameFilter, 
                                 @Param("store") String store, 
                                 @Param("month") Integer month);

    /**
     * Retrieves aggregated user productivity data.
     * This query calculates various metrics to measure user performance.
     * @return A list of Object arrays containing productivity metrics for each user
     */
    @Query("SELECT p.user.username, " +
           "COUNT(p), " +
           "COALESCE(SUM(p.pouchesChecked), 0), " +
           "CASE WHEN SUM(p.pouchesChecked) > 0 " +
           "     THEN CAST(SUM(FUNCTION('TIMESTAMPDIFF', SECOND, p.startTime, p.endTime)) AS double) / SUM(p.pouchesChecked) " +
           "     ELSE 0.0 END, " +
           "CASE WHEN SUM(FUNCTION('TIMESTAMPDIFF', HOUR, p.startTime, p.endTime)) > 0 " +
           "     THEN CAST(SUM(p.pouchesChecked) AS double) / SUM(FUNCTION('TIMESTAMPDIFF', HOUR, p.startTime, p.endTime)) " +
           "     ELSE 0.0 END " +
           "FROM Pac p " +
           "GROUP BY p.user.username")
    List<Object[]> getUserProductivityData();

    // TODO: Consider adding methods for more specific queries, such as finding Pacs by date range
    // TODO: Implement query method for calculating average pouches checked per hour for a specific user
}