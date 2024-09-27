package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;
import jakarta.transaction.Transactional;
import java.util.*;
import java.time.Duration;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class UserProductivityService {

    private final PacRepository pacRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @PersistenceContext
    private EntityManager entityManager;

    public UserProductivityService(PacRepository pacRepository, ObjectMapper objectMapper) {
        this.pacRepository = pacRepository;
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribeToProductivityUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
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

        try {
            List<UserProductivityDTO> userProductivity = getUserProductivityData();
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(userProductivity)));
        } catch (IOException e) {
            logger.error("Error sending initial data", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public SseEmitter subscribeToOverallProductivityUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Similar setup as in subscribeToProductivityUpdates
        // ...

        try {
            UserProductivityDTO overallProductivity = getOverallProductivity();
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(overallProductivity)));
        } catch (IOException e) {
            logger.error("Error sending initial overall productivity data", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Cacheable(value = "allUserProductivity", key = "#page + '-' + #size")
    @Transactional
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data for page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = pacRepository.getUserProductivityDataPaginated(pageable);
        return results.map(this::mapToUserProductivityDTO);
    }

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

    private UserProductivityDTO mapToUserProductivityDTO(Map.Entry<String, List<Pac>> entry) {
        String username = entry.getKey();
        List<Pac> userPacs = entry.getValue();
        
        long totalSubmissions = userPacs.size();
        long totalPouchesChecked = userPacs.stream().mapToLong(Pac::getPouchesChecked).sum();
        long totalMinutes = userPacs.stream()
            .mapToLong(pac -> Duration.between(pac.getStartTime(), pac.getEndTime()).toMinutes())
            .sum();

        double avgPouchesPerHour = totalMinutes > 0 ? (totalPouchesChecked * 60.0) / totalMinutes : 0;
        String avgTimeDuration = formatDuration(totalSubmissions > 0 ? totalMinutes / totalSubmissions : 0);

        return new UserProductivityDTO(username, totalSubmissions, totalPouchesChecked, avgTimeDuration, avgPouchesPerHour);
    }

    private UserProductivityDTO calculateOverallProductivity(List<UserProductivityDTO> allProductivity) {
        long totalSubmissions = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalSubmissions).sum();
        long totalPouchesChecked = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalPouchesChecked).sum();
        double avgPouchesPerHour = allProductivity.stream().mapToDouble(UserProductivityDTO::getAvgPouchesPerHour).average().orElse(0.0);
        String avgTimeDuration = calculateAverageTimeDuration(allProductivity);

        return new UserProductivityDTO("Overall", totalSubmissions, totalPouchesChecked, avgTimeDuration, avgPouchesPerHour);
    }

    private String calculateAverageTimeDuration(List<UserProductivityDTO> allProductivity) {
        long totalMinutes = allProductivity.stream()
            .mapToLong(dto -> {
                String[] parts = dto.getAvgTimeDuration().split(":");
                return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            })
            .sum();

        return formatDuration(allProductivity.isEmpty() ? 0 : totalMinutes / allProductivity.size());
    }

    public void notifyProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

    public void sendOverallProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        sendUpdateToEmitters(overallProductivity);
    }

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

    public List<UserProductivityDTO> getUserProductivityData() {
        List<Object[]> results = pacRepository.getUserProductivityData();
        return results.stream()
            .map(this::mapToUserProductivityDTO)
            .filter(dto -> !"Overall".equals(dto.getUsername()))
            .collect(Collectors.toList());
    }

    private UserProductivityDTO mapToUserProductivityDTO(Object[] result) {
        return new UserProductivityDTO(
            (String) result[0],
            ((Number) result[1]).longValue(),
            ((Number) result[2]).longValue(),
            formatDuration(((Number) result[3]).doubleValue()),
            ((Number) result[4]).doubleValue()
        );
    }

    private String formatDuration(double hours) {
        long totalMinutes = (long) (hours * 60);
        return String.format("%d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

    public Map<String, Object> getUserProductivity(String username) {
        String jpql = "SELECT COUNT(p) as totalSubmissions, " +
                      "SUM(p.pouchesChecked) as totalPouchesChecked, " +
                      "SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, p.startTime, p.endTime)) as totalMinutes " +
                      "FROM Pac p WHERE p.user.username = :username";
        
        Object[] result = (Object[]) entityManager.createQuery(jpql)
                .setParameter("username", username)
                .getSingleResult();
        
        long totalSubmissions = ((Number) result[0]).longValue();
        long totalPouchesChecked = ((Number) result[1]).longValue();
        long totalMinutes = ((Number) result[2]).longValue();
        
        double avgPouchesPerHour = totalMinutes > 0 ? (totalPouchesChecked * 60.0) / totalMinutes : 0;
        
        return Map.of(
            "totalSubmissions", totalSubmissions,
            "totalPouchesChecked", totalPouchesChecked,
            "avgPouchesPerHour", avgPouchesPerHour,
            "avgTimeDuration", formatDuration(totalMinutes / 60.0)
        );
    }

    @CacheEvict(value = "allUserProductivity", allEntries = true)
    @Transactional
    public void updateUserProductivity() {
        logger.info("Updating user productivity and evicting cache");
        // Add any necessary update logic here
    }

    public void sendProductivityUpdate() {
        List<UserProductivityDTO> users = getUserProductivityData();
        sendUpdateToEmitters(users);
    }
}