package com.demoproject.demo.repository;

import com.demoproject.demo.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /* -----------------------------------------------------------------------------
     * Core Audit Queries
     * -------------------------------------------------------------------------- */
    
    /**
     * Retrieves paginated audit logs ordered by timestamp
     * 
     * @param pageable Pagination parameters
     * @returns Page<AuditLog> - Empty if no logs exist
     * @note Default sort by timestamp descending
     * @performance Uses database-level pagination
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Finds audit logs for specific user
     * 
     * @param performedBy Username of the action performer
     * @param pageable Pagination parameters
     * @returns Page<AuditLog> - User's audit trail
     */
    Page<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy, Pageable pageable);

    /**
     * Retrieves logs by category with pagination
     * 
     * @param category Action category to filter
     * @param pageable Pagination parameters
     * @returns Page<AuditLog> - Categorized audit entries
     */
    Page<AuditLog> findByCategoryOrderByTimestampDesc(String category, Pageable pageable);

    /**
     * Finds logs within a date range
     * 
     * @param startDate Range start (inclusive)
     * @param endDate Range end (inclusive)
     * @returns List<AuditLog> - Matching audit entries
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Retrieves recent actions by user
     * 
     * @param performedBy Username to filter
     * @param limit Maximum number of records
     * @returns List<AuditLog> - Recent user actions
     */
    @Query(value = "SELECT a FROM AuditLog a WHERE a.performedBy = :performedBy " +
           "ORDER BY a.timestamp DESC LIMIT :limit")
    List<AuditLog> findRecentActionsByUser(
        @Param("performedBy") String performedBy,
        @Param("limit") int limit
    );

    /* -----------------------------------------------------------------------------
     * Future Enhancements
     * -------------------------------------------------------------------------- */

    /* @todo Implementation needs:
     * - Add full-text search capabilities
     * - Implement audit log archiving
     * - Add aggregation queries for reporting
     * - Consider adding custom projections
     * - Add support for audit log rotation
     */
}
