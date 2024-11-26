package com.demoproject.demo.pacmedproductivity;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

/**
 * Service class for managing user productivity data and real-time updates.
 * This class handles calculations, caching, and Server-Sent Events (SSE) for productivity metrics.
 */
@Service
public class UserProductivityService {

    private final PacRepository pacRepository;
    
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    public static final long SSE_TIMEOUT = 300000L; // 5 minutes

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Constructs a new UserProductivityService with necessary dependencies.
     *
     * @param pacRepository Repository for PAC (Pouch Accuracy Check) data
     * @param objectMapper JSON object mapper for data serialization
     */
    public UserProductivityService(PacRepository pacRepository, ObjectMapper objectMapper) {
        this.pacRepository = pacRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Establishes an SSE connection for real-time productivity updates.
     *
     * @return SseEmitter for the established connection
     */
    @Transactional
    public SseEmitter subscribeToProductivityUpdates(SseEmitter emitter) {
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
     * Establishes an SSE connection for overall productivity updates.
     *
     * @return SseEmitter for the established connection
     */
    public SseEmitter subscribeToOverallProductivityUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Similar setup as in subscribeToProductivityUpdates
        // ...

        // Send initial overall productivity data
        try {
            UserProductivityDTO overallProductivity = getOverallProductivity();
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(overallProductivity)));
        } catch (IOException e) {
            logger.error("Error sending initial overall productivity data", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Retrieves paginated user productivity data with caching.
     *
     * @param page Page number
     * @param size Page size
     * @return Page of UserProductivityDTO objects
     */
    @Cacheable(value = "allUserProductivity", key = "#page + '-' + #size")
    @Transactional
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data for page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return pacRepository.getUserProductivityDataPaginated(pageable)
            .map(this::mapToUserProductivityDTO);
    }

    /**
     * Calculates and retrieves overall productivity metrics.
     *
     * @return UserProductivityDTO representing overall productivity
     */
    @Transactional
    public UserProductivityDTO getOverallProductivity() {
        List<Pac> allPacs = pacRepository.findAll();
        List<UserProductivityDTO> allProductivity = allPacs.stream()
            .collect(Collectors.groupingBy(pac -> pac.getUser().getUsername()))
            .entrySet().stream()
            .map(this::mapToUserProductivityDTO)
            .collect(Collectors.toList());

        return calculateOverallProductivity(allProductivity);
    }

    // Helper methods for DTO mapping and calculations
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

    private UserProductivityDTO calculateOverallProductivity(List<UserProductivityDTO> allProductivity) {
        long totalSubmissions = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalSubmissions).sum();
        long totalPouchesChecked = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalPouchesChecked).sum();
        double avgTimePerPouch = allProductivity.stream().mapToDouble(dto -> dto.getAvgTimePerPouch() * dto.getTotalPouchesChecked()).sum() / totalPouchesChecked;
        double avgPouchesPerHour = allProductivity.stream().mapToDouble(UserProductivityDTO::getAvgPouchesPerHour).average().orElse(0.0);

        return new UserProductivityDTO("Overall", totalSubmissions, totalPouchesChecked, avgTimePerPouch, avgPouchesPerHour, null);
    }

    /**
     * Notifies all connected clients about productivity updates.
     */
    public void notifyProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

    /**
     * Sends overall productivity update to all connected clients.
     */
    public void sendOverallProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

    /**
     * Sends updates to all connected SSE clients.
     *
     * @param data Data to be sent to clients
     */
    private void sendUpdateToEmitters(Object data) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                emitter.send(SseEmitter.event().data(jsonData));
                // Send heartbeat
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                deadEmitters.add(emitter);
                logger.warn("Failed to send update to emitter", e);
            }
        });

        // Clean up dead emitters
        emitters.removeAll(deadEmitters);
    }

    // Helper methods for DTO mapping and formatting
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

    /**
     * Retrieves productivity data for a specific user.
     *
     * @param username Username of the user
     * @return Map containing productivity metrics
     */
    public Map<String, Object> getUserProductivity(String username) {
        String jpql = "SELECT COUNT(p) as totalSubmissions, " +
                      "SUM(p.pouchesChecked) as totalPouchesChecked, " +
                      "SUM(FUNCTION('TIMESTAMPDIFF', SECOND, p.startTime, p.endTime)) as totalSeconds " +
                      "FROM Pac p WHERE p.user.username = :username";
        
        Object[] result = (Object[]) entityManager.createQuery(jpql)
                .setParameter("username", username)
                .getSingleResult();
        
        // Calculate metrics
        long totalSubmissions = ((Number) result[0]).longValue();
        long totalPouchesChecked = ((Number) result[1]).longValue();
        double totalSeconds = ((Number) result[2]).doubleValue();
        
        double avgTimePerPouch = totalPouchesChecked > 0 ? totalSeconds / totalPouchesChecked : 0;
        double avgPouchesPerHour = totalSeconds > 0 ? (totalPouchesChecked * 3600.0) / totalSeconds : 0;
        
        // Return productivity metrics
        return Map.of(
            "totalSubmissions", totalSubmissions,
            "totalPouchesChecked", totalPouchesChecked,
            "avgPouchesPerHour", avgPouchesPerHour,
            "avgTimePerPouch", avgTimePerPouch
        );
    }

    /**
     * Updates user productivity and evicts the cache.
     */
    @CacheEvict(value = "allUserProductivity", allEntries = true)
    @Transactional
    public void updateUserProductivity() {
        logger.info("Updating user productivity and evicting cache");
        // Add any necessary update logic here
    }

    /**
     * Sends productivity updates to all connected clients.
     */
    public void sendProductivityUpdate() {
        Page<UserProductivityDTO> users = getAllUserProductivity(0, Integer.MAX_VALUE);
        sendUpdateToEmitters(users);
    }

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

    /**
     * Retrieves productivity data for a specific user.
     *
     * @param username The username of the user
     * @return A map containing productivity metrics for the user
     */
    public Map<String, Object> getUserProductivityMetrics(String username) {
        logger.info("Retrieving productivity data for user: {}", username);
        return getUserProductivity(username);
    }

    /**
     * Retrieves productivity data for all users.
     *
     * @return A list of UserProductivityDTO objects containing productivity data for all users
     */
    public List<UserProductivityDTO> getAllUserProductivityMetrics() {
        logger.info("Retrieving productivity data for all users.");
        return getAllUserProductivity(0, Integer.MAX_VALUE).getContent();
    }

    /**
     * Retrieves paginated productivity data for all users.
     *
     * @param page The page number (zero-based)
     * @param size The size of each page
     * @return A Page of UserProductivityDTO objects
     */
    public Page<UserProductivityDTO> getAllUserProductivityMetrics(int page, int size) {
        logger.info("Retrieving productivity data for all users. Page: {}, Size: {}", page, size);
        return getAllUserProductivity(page, size);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        logger.debug("Removed SSE emitter");
    }

    /**
     * Checks if a user exists in the system.
     *
     * @param username The username to check
     * @return boolean indicating if the user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        try {
            String jpql = "SELECT COUNT(p) > 0 FROM Pac p WHERE p.user.username = :username";
            return entityManager.createQuery(jpql, Boolean.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            logger.error("Error checking user existence for username: {}", username, e);
            return false;
        }
    }
}
