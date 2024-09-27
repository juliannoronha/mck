package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserRepository;
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
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;

@Service
@Configuration
public class UserProductivityService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PacRepository pacRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Autowired
    private EntityManager entityManager;

    public UserProductivityService(PacRepository pacRepository) {
        this.pacRepository = pacRepository;
    }

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    @Cacheable(value = "allUserProductivity", keyGenerator = "customKeyGenerator")
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data for page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = pacRepository.getUserProductivityDataPaginated(pageable);
        return results.map(this::mapToUserProductivityDTO);
    }

    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(param.toString());
            }
            return sb.toString();
        };
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

        // Add a check to prevent division by zero
        double avgPouchesPerHour = totalMinutes > 0 ? (totalPouchesChecked * 60.0) / totalMinutes : 0;
        String avgTimeDuration = totalSubmissions > 0 
            ? String.format("%d:%02d", totalMinutes / totalSubmissions / 60, 
                            totalMinutes / totalSubmissions % 60)
            : "0:00";

        return new UserProductivityDTO(
            username,
            totalSubmissions,
            totalPouchesChecked,
            avgTimeDuration,
            avgPouchesPerHour
        );
    }

    private UserProductivityDTO calculateOverallProductivity(List<UserProductivityDTO> allProductivity) {
        long totalSubmissions = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalSubmissions).sum();
        long totalPouchesChecked = allProductivity.stream().mapToLong(UserProductivityDTO::getTotalPouchesChecked).sum();
        double avgPouchesPerHour = allProductivity.stream().mapToDouble(UserProductivityDTO::getAvgPouchesPerHour).average().orElse(0.0);
        
        String avgTimeDuration = calculateAverageTimeDuration(allProductivity);

        return new UserProductivityDTO(
            "Overall",
            totalSubmissions,
            totalPouchesChecked,
            avgTimeDuration,
            avgPouchesPerHour
        );
    }

    private String calculateAverageTimeDuration(List<UserProductivityDTO> allProductivity) {
        long totalMinutes = allProductivity.stream()
            .mapToLong(dto -> {
                String[] parts = dto.getAvgTimeDuration().split(":");
                return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            })
            .sum();

        long avgMinutes = allProductivity.isEmpty() ? 0 : totalMinutes / allProductivity.size();
        return String.format("%d:%02d", avgMinutes / 60, avgMinutes % 60);
    }

    public void notifyProductivityUpdate() {
        List<UserProductivityDTO> updatedData = getUserProductivityData();
        logger.info("Notifying {} emitters of productivity update", emitters.size());
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(updatedData));
                return false;
            } catch (IOException e) {
                logger.error("Error sending SSE update", e);
                return true;
            }
        });
    }

    public void sendOverallProductivityUpdate() {
        UserProductivityDTO overallProductivity = getOverallProductivity();
        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(overallProductivity));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }

    public List<UserProductivityDTO> getUserProductivityData() {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
            .map(user -> {
                List<Pac> userPacs = pacRepository.findByUser_Username(user.getUsername());
                return mapToUserProductivityDTO(Map.entry(user.getUsername(), userPacs));
            })
            .collect(Collectors.toList());
    }

    private UserProductivityDTO mapToUserProductivityDTO(Object[] result) {
        String username = (String) result[0];
        long totalSubmissions = ((Number) result[1]).longValue();
        String avgTimeDuration = (String) result[2];
        double avgPouchesPerHour = result[3] instanceof Number ? ((Number) result[3]).doubleValue() : 0.0;
        long totalPouchesChecked = ((Number) result[4]).longValue();

        logger.debug("Mapping user productivity: username={}, totalSubmissions={}, avgTimeDuration={}, avgPouchesPerHour={}, totalPouchesChecked={}",
                     username, totalSubmissions, avgTimeDuration, avgPouchesPerHour, totalPouchesChecked);

        return new UserProductivityDTO(
            username,
            totalSubmissions,
            totalPouchesChecked,
            avgTimeDuration,
            avgPouchesPerHour
        );
    }

    // Call this method whenever the productivity data changes
    public void updateProductivity() {
        sendOverallProductivityUpdate();
    }

    public Map<String, Object> getUserProductivity(String username) {
        String jpql = "SELECT COUNT(p) as totalSubmissions, " +
                      "SUM(p.pouchesChecked) as totalPouchesChecked, " +
                      "SUM(FUNCTION('TIMESTAMPDIFF', MINUTE, p.startTime, p.endTime)) as totalMinutes " +
                      "FROM Pac p WHERE p.user.username = :username";
        
        Object[] result = (Object[]) entityManager.createQuery(jpql)
                .setParameter("username", username)
                .getSingleResult();
        
        Map<String, Object> productivityMap = new HashMap<>();
        productivityMap.put("totalSubmissions", ((Number) result[0]).longValue());
        productivityMap.put("totalPouchesChecked", ((Number) result[1]).longValue());
        long totalMinutes = ((Number) result[2]).longValue();
        
        double avgPouchesPerHour = totalMinutes > 0 ? (((Number) result[1]).doubleValue() * 60.0) / totalMinutes : 0;
        
        productivityMap.put("avgPouchesPerHour", avgPouchesPerHour);
        productivityMap.put("avgTimeDuration", String.format("%d:%02d", totalMinutes / 60, totalMinutes % 60));
        
        return productivityMap;
    }
}
