package com.demoproject.demo.services;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    public SseEmitter subscribeToProductivityUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Setup completion and timeout handlers
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("SSE connection closed");
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.info("SSE connection timed out");
        });

        emitters.add(emitter);
        logger.info("New SSE connection established");

        // Send initial data
        try {
            Page<UserProductivityDTO> userProductivity = getAllUserProductivity(0, Integer.MAX_VALUE);
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(userProductivity.getContent())));
        } catch (IOException e) {
            logger.error("Error sending initial data", e);
            emitter.completeWithError(e);
        }

        return emitter;
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
        Page<Object[]> results = pacRepository.getUserProductivityDataPaginated(pageable);
        return results.map(this::mapToUserProductivityDTO);
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

        return new UserProductivityDTO(username, totalSubmissions, totalPouchesChecked, avgTimePerPouch, avgPouchesPerHour);
    }

    private UserProductivityDTO calculateOverallProductivity(List<UserProductivityDTO> allProductivity) {
        long totalSubmissions = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalSubmissions).sum();
        long totalPouchesChecked = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalPouchesChecked).sum();
        double avgTimePerPouch = allProductivity.stream().mapToDouble(dto -> dto.getAvgTimePerPouch() * dto.getTotalPouchesChecked()).sum() / totalPouchesChecked;
        double avgPouchesPerHour = allProductivity.stream().mapToDouble(UserProductivityDTO::getAvgPouchesPerHour).average().orElse(0.0);

        return new UserProductivityDTO("Overall", totalSubmissions, totalPouchesChecked, avgTimePerPouch, avgPouchesPerHour);
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
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            emitters.removeIf(emitter -> {
                try {
                    emitter.send(jsonData);
                    return false;
                } catch (IOException e) {
                    logger.error("Error sending SSE update", e);
                    return true;
                }
            });
        } catch (JsonProcessingException e) {
            logger.error("Error converting data to JSON", e);
        }
    }

    // Helper methods for DTO mapping and formatting
    private UserProductivityDTO mapToUserProductivityDTO(Object[] result) {
        return new UserProductivityDTO(
            (String) result[0],
            ((Number) result[1]).longValue(),
            ((Number) result[2]).longValue(),
            ((Number) result[3]).doubleValue(), // This is now avgTimePerPouch in seconds
            ((Number) result[4]).doubleValue()
        );
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
}