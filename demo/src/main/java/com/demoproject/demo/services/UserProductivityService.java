package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.cache.annotation.Cacheable;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalTime;
import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserProductivityService {
    private final UserAnswerRepository userAnswerRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);

    public UserProductivityService(UserAnswerRepository userAnswerRepository) {
        this.userAnswerRepository = userAnswerRepository;
    }

    public Map<String, Object> getUserProductivity(String username) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByName(username);
        
        if (userAnswers.isEmpty()) {
            return Collections.emptyMap();
        }

        int totalPouches = userAnswers.stream().mapToInt(UserAnswer::getPouchesChecked).sum();
        long totalMinutes = userAnswers.stream()
            .mapToLong(answer -> {
                LocalTime startTime = answer.getStartTime();
                LocalTime endTime = answer.getEndTime();
                return Duration.between(startTime, endTime).toMinutes();
            })
            .sum();

        double avgPouchesPerHour = totalMinutes > 0 ? (totalPouches * 60.0) / totalMinutes : 0;

        Map<String, Object> productivity = new HashMap<>();
        productivity.put("username", username);
        productivity.put("totalSubmissions", userAnswers.size());
        productivity.put("totalPouchesChecked", totalPouches);
        productivity.put("totalMinutesWorked", totalMinutes);
        productivity.put("avgPouchesPerHour", avgPouchesPerHour);

        return productivity;
    }

    @Cacheable("allUserProductivity")
    public List<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data");
        return userAnswerRepository.getUserProductivityData();
    }

    public UserProductivityDTO getOverallProductivity() {
        List<UserAnswer> allAnswers = userAnswerRepository.findAll();
        
        int totalSubmissions = allAnswers.size();
        int totalPouchesChecked = allAnswers.stream().mapToInt(UserAnswer::getPouchesChecked).sum();
        long totalMinutes = allAnswers.stream()
            .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
            .sum();

        double avgPouchesPerHour = totalMinutes > 0 ? (totalPouchesChecked * 60.0) / totalMinutes : 0;
        String avgTimeDuration = String.format("%d:%02d", totalMinutes / (totalSubmissions > 0 ? totalSubmissions : 1) / 60, 
                                               totalMinutes / (totalSubmissions > 0 ? totalSubmissions : 1) % 60);
        double avgPouchesChecked = totalSubmissions > 0 ? (double) totalPouchesChecked / totalSubmissions : 0;

        return new UserProductivityDTO(
            "Overall",
            (long) totalSubmissions,  // Cast to Long
            avgTimeDuration,
            avgPouchesPerHour,
            (long) totalPouchesChecked,  // Cast to Long
            avgPouchesChecked
        );
    }

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    public void notifyProductivityUpdate() {
        logger.info("Notifying productivity update to {} emitters", emitters.size());
        List<UserProductivityDTO> updatedData = getAllUserProductivity(0, Integer.MAX_VALUE);
        logger.info("Updated data: {}", updatedData); // Add this line
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(updatedData));
                logger.debug("Sent update to emitter");
            } catch (IOException e) {
                logger.error("Error sending SSE update", e);
                emitters.remove(emitter);
            }
        });
    }

    public void analyzeUserProductivityQuery() {
        // This method might need to be updated or removed if it's no longer applicable
        logger.info("Analyzing user productivity query");
        // Implement appropriate logic here if needed
    }
}
