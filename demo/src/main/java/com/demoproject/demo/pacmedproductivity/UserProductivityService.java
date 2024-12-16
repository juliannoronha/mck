/* =============================================================================
 * User Productivity Service
 * =============================================================================
 * PURPOSE: Manages user productivity metrics, caching, and real-time updates
 * DEPENDENCIES: 
 * - Spring Framework
 * - Jackson ObjectMapper
 * - JPA/Hibernate
 * - Server-Sent Events (SSE)
 * 
 * @author DemoProject Team
 * @version 1.0
 */
package com.demoproject.demo.pacmedproductivity;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/* -----------------------------------------------------------------------------
 * Core Service Configuration
 * -------------------------------------------------------------------------- */
@Service
public class UserProductivityService {

    private final PacRepository pacRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    public static final long SSE_TIMEOUT = 300000L; // 5 minutes

    /**
     * Service constructor
     * @param pacRepository Data access for PAC records
     * @param objectMapper JSON serialization
     */
    public UserProductivityService(PacRepository pacRepository, ObjectMapper objectMapper) {
        this.pacRepository = pacRepository;
        this.objectMapper = objectMapper;
    }

    /* -----------------------------------------------------------------------------
     * SSE Connection Management 
     * -------------------------------------------------------------------------- */

    /**
     * Establishes SSE connection for real-time updates
     * @param emitter SSE emitter instance
     * @returns Configured emitter
     * @throws IOException on connection errors
     */
    @Transactional
    public SseEmitter subscribeToProductivityUpdates(SseEmitter emitter) {
        // Configure emitter lifecycle handlers
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onError(e -> {
            emitters.remove(emitter);
            emitter.completeWithError(e);
        });
        
        try {
            emitters.add(emitter);
            
            // Send initial data
            Page<UserProductivityDTO> initialData = getAllUserProductivity(0, Integer.MAX_VALUE);
            String jsonData = objectMapper.writeValueAsString(initialData.getContent());
            emitter.send(SseEmitter.event().data(jsonData));
            
            return emitter;
        } catch (Exception e) {
            logger.error("Error setting up SSE connection: {}", e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }
    }

    /**
     * Establishes SSE connection for overall metrics
     * @returns Configured emitter for overall updates
     */
    public SseEmitter subscribeToOverallProductivityUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        try {
            UserProductivityDTO overallProductivity = getOverallProductivity();
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(overallProductivity)));
        } catch (IOException e) {
            logger.error("Error sending initial overall productivity data", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /* -----------------------------------------------------------------------------
     * Productivity Data Access
     * -------------------------------------------------------------------------- */

    /**
     * Retrieves paginated productivity data
     * @param page Zero-based page number
     * @param size Results per page
     * @returns Page of productivity DTOs
     * @note Results are cached by page/size
     */
    @Cacheable(value = "allUserProductivity", key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data for page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return pacRepository.getUserProductivityDataPaginated(pageable)
            .map(this::mapToUserProductivityDTO);
    }

    /**
     * Calculates overall productivity metrics
     * @returns Aggregated productivity DTO
     */
    @Transactional(readOnly = true)
    public UserProductivityDTO getOverallProductivity() {
        List<Pac> allPacs = pacRepository.findAll();
        List<UserProductivityDTO> allProductivity = allPacs.stream()
            .collect(Collectors.groupingBy(pac -> pac.getUser().getUsername()))
            .entrySet().stream()
            .map(this::mapToUserProductivityDTO)
            .collect(Collectors.toList());

        return calculateOverallProductivity(allProductivity);
    }

    /* -----------------------------------------------------------------------------
     * Data Mapping & Calculations
     * -------------------------------------------------------------------------- */

    /**
     * Maps PAC records to productivity DTO
     * @param entry Map entry of username and PAC records
     * @returns Mapped productivity DTO
     */
    private UserProductivityDTO mapToUserProductivityDTO(Map.Entry<String, List<Pac>> entry) {
        String username = entry.getKey();
        List<Pac> userPacs = entry.getValue();
        
        long totalSubmissions = userPacs.size();
        long totalPouchesChecked = userPacs.stream().mapToLong(Pac::getPouchesChecked).sum();
        double totalSeconds = userPacs.stream()
            .mapToDouble(pac -> Duration.between(pac.getStartTime(), pac.getEndTime()).getSeconds())
            .sum();

        double avgTimePerPouch = totalPouchesChecked > 0 ? totalSeconds / totalPouchesChecked : 0;
        double avgPouchesPerHour = totalSeconds > 0 ? (totalPouchesChecked * 3600.0) / totalSeconds : 0;

        return new UserProductivityDTO(username, totalSubmissions, totalPouchesChecked, avgTimePerPouch, avgPouchesPerHour, null);
    }

    /**
     * Calculates overall productivity from individual metrics
     * @param allProductivity List of individual productivity DTOs
     * @returns Aggregated productivity DTO
     */
    private UserProductivityDTO calculateOverallProductivity(List<UserProductivityDTO> allProductivity) {
        long totalSubmissions = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalSubmissions).sum();
        long totalPouchesChecked = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalPouchesChecked).sum();
        double avgTimePerPouch = allProductivity.stream().mapToDouble(dto -> dto.getAvgTimePerPouch() * dto.getTotalPouchesChecked()).sum() / totalPouchesChecked;
        double avgPouchesPerHour = allProductivity.stream().mapToDouble(UserProductivityDTO::getAvgPouchesPerHour).average().orElse(0.0);

        return new UserProductivityDTO("Overall", totalSubmissions, totalPouchesChecked, avgTimePerPouch, avgPouchesPerHour, null);
    }

    /* -----------------------------------------------------------------------------
     * Real-time Update Notifications
     * -------------------------------------------------------------------------- */

    /**
     * Broadcasts productivity updates to all clients
     */
    public void notifyProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

    /**
     * Broadcasts overall metrics to all clients
     */
    public void sendOverallProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

    /**
     * Sends data to all connected SSE clients
     * @param data Data to broadcast
     * @note Handles client disconnections
     */
    private void sendUpdateToEmitters(Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                emitter.send(SseEmitter.event().data(jsonData));
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize productivity data: {}", e.getMessage());
                deadEmitters.add(emitter);
            } catch (IOException e) {
                logger.error("Failed to send SSE update: {}", e.getMessage());
                deadEmitters.add(emitter);
            } catch (Exception e) {
                logger.error("Unexpected error during SSE update: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }

    /* -----------------------------------------------------------------------------
     * Data Transformation
     * -------------------------------------------------------------------------- */

    /**
     * Maps query results to productivity DTO
     * @param result Query result array
     * @returns Mapped productivity DTO
     * @note Handles null/invalid data
     */
    private UserProductivityDTO mapToUserProductivityDTO(Object[] result) {
        try {
            String username = (String) result[0];
            long totalSubmissions = ((Number) result[1]).longValue();
            long totalPouchesChecked = ((Number) result[2]).longValue();
            double avgTimePerPouch = ((Number) result[3]).doubleValue();
            double avgPouchesPerHour = ((Number) result[4]).doubleValue();
            
            logger.debug("Mapping productivity data for user: {}", username);
            
            return new UserProductivityDTO(
                username,
                totalSubmissions,
                totalPouchesChecked,
                avgTimePerPouch,
                avgPouchesPerHour,
                null
            );
        } catch (Exception e) {
            logger.error("Error mapping productivity data: {}", e.getMessage());
            return new UserProductivityDTO(
                "Unknown",
                0L,
                0L,
                0.0,
                0.0,
                null
            );
        }
    }

    /* -----------------------------------------------------------------------------
     * User-specific Productivity
     * -------------------------------------------------------------------------- */

    /**
     * Gets productivity metrics for specific user
     * @param username Target username
     * @returns Map of productivity metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserProductivity(String username) {
        try {
            Object[] result = pacRepository.getUserProductivityMetrics(username);
            
            // Calculate metrics
            long totalSubmissions = ((Number) result[0]).longValue();
            long totalPouchesChecked = ((Number) result[1]).longValue();
            double totalSeconds = ((Number) result[2]).doubleValue();
            
            double avgTimePerPouch = totalPouchesChecked > 0 ? totalSeconds / totalPouchesChecked : 0;
            double avgPouchesPerHour = totalSeconds > 0 ? (totalPouchesChecked * 3600.0) / totalSeconds : 0;
            
            return Map.of(
                "totalSubmissions", totalSubmissions,
                "totalPouchesChecked", totalPouchesChecked,
                "avgPouchesPerHour", avgPouchesPerHour,
                "avgTimePerPouch", avgTimePerPouch
            );
        } catch (Exception e) {
            logger.error("Error fetching productivity metrics for user: {}", username, e);
            throw new RuntimeException("Failed to fetch user productivity metrics", e);
        }
    }

    /* -----------------------------------------------------------------------------
     * Cache Management
     * -------------------------------------------------------------------------- */

    /**
     * Updates productivity and invalidates cache
     */
    @CacheEvict(value = "allUserProductivity", allEntries = true)
    @Transactional
    public void updateUserProductivity() {
        logger.info("Updating user productivity and evicting cache");
    }

    /* -----------------------------------------------------------------------------
     * Productivity Updates
     * -------------------------------------------------------------------------- */

    /**
     * Broadcasts productivity data to all clients
     */
    public void sendProductivityUpdate() {
        Page<UserProductivityDTO> users = getAllUserProductivity(0, Integer.MAX_VALUE);
        sendUpdateToEmitters(users);
    }

    /**
     * Cleanup on service shutdown
     */
    @PreDestroy
    public void cleanup() {
        emitters.forEach(emitter -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error during emitter cleanup", e);
            }
        });
        emitters.clear();
    }

    /* -----------------------------------------------------------------------------
     * Public API Methods
     * -------------------------------------------------------------------------- */

    /**
     * Gets user-specific productivity metrics
     * @param username Target username
     * @returns Map of productivity metrics
     */
    public Map<String, Object> getUserProductivityMetrics(String username) {
        logger.info("Retrieving productivity data for user: {}", username);
        return getUserProductivity(username);
    }

    /**
     * Gets productivity data for all users
     * @returns List of productivity DTOs
     */
    public List<UserProductivityDTO> getAllUserProductivityMetrics() {
        logger.info("Retrieving productivity data for all users.");
        return getAllUserProductivity(0, Integer.MAX_VALUE).getContent();
    }

    /**
     * Gets paginated productivity data
     * @param page Page number (zero-based)
     * @param size Page size
     * @returns Page of productivity DTOs
     */
    public Page<UserProductivityDTO> getAllUserProductivityMetrics(int page, int size) {
        logger.info("Retrieving productivity data for all users. Page: {}, Size: {}", page, size);
        return getAllUserProductivity(page, size);
    }

    /**
     * Removes SSE emitter
     * @param emitter Emitter to remove
     */
    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        logger.debug("Removed SSE emitter");
    }

    /**
     * Checks if user exists
     * @param username Username to check
     * @returns True if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        try {
            return pacRepository.existsByUsername(username);
        } catch (Exception e) {
            logger.error("Error checking user existence for username: {}", username, e);
            return false;
        }
    }
}
