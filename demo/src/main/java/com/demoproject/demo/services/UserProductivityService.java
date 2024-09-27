package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserAnswerRepository;
import com.demoproject.demo.repository.PacRepository;

import java.util.*;
import java.time.Duration;
import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserProductivityService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PacRepository pacRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final UserAnswerRepository userAnswerRepository;

    public UserProductivityService(UserAnswerRepository userAnswerRepository, PacRepository pacRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.pacRepository = pacRepository;
    }

    public void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public Map<String, Object> getUserProductivity(String username) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByUser_Username(username);
        
        if (userAnswers.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Pac> pacs = userAnswers.stream()
            .map(UserAnswer::getPac)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        int totalPouches = pacs.stream().mapToInt(Pac::getPouchesChecked).sum();
        long totalMinutes = pacs.stream()
            .mapToLong(pac -> Duration.between(pac.getStartTime(), pac.getEndTime()).toMinutes())
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
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Fetching all user productivity data for page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = userAnswerRepository.getUserProductivityDataPaginated(pageable);
        return results.map(this::mapToUserProductivityDTO);
    }

    @Transactional
    public UserProductivityDTO getOverallProductivity() {
        List<UserAnswer> allUserAnswers = userAnswerRepository.findAll();
        List<UserProductivityDTO> allProductivity = allUserAnswers.stream()
            .map(this::mapToUserProductivityDTO)
            .collect(Collectors.toList());

        return calculateOverallProductivity(allProductivity);
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
        List<SseEmitter> deadEmitters = new ArrayList<>();
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(updatedData));
                logger.debug("Sent update to emitter");
            } catch (IOException e) {
                logger.error("Error sending SSE update", e);
                deadEmitters.add(emitter);
            }
        });
        emitters.removeAll(deadEmitters);
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
        List<UserProductivityDTO> productivityData = new ArrayList<>();
        List<User> usersWithSubmissions = userRepository.findUsersWithSubmissions();

        for (User user : usersWithSubmissions) {
            List<Pac> userPacs = pacRepository.findByUserAnswer_User_Username(user.getUsername());
            
            int totalSubmissions = userPacs.size();
            int totalPouchesChecked = userPacs.stream().mapToInt(Pac::getPouchesChecked).sum();
            long totalMinutes = userPacs.stream()
                .mapToLong(pac -> Duration.between(pac.getStartTime(), pac.getEndTime()).toMinutes())
                .sum();

            double avgPouchesPerHour = totalMinutes > 0 ? (totalPouchesChecked * 60.0) / totalMinutes : 0;
            String avgTimeDuration = String.format("%d:%02d", totalMinutes / totalSubmissions / 60, 
                                                   totalMinutes / totalSubmissions % 60);

            productivityData.add(new UserProductivityDTO(
                user.getUsername(),
                (long) totalSubmissions,
                (long) totalPouchesChecked,
                avgTimeDuration,
                avgPouchesPerHour
            ));
        }

        return productivityData;
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

    private UserProductivityDTO mapToUserProductivityDTO(UserAnswer userAnswer) {
        Pac pac = userAnswer.getPac();
        if (pac == null) {
            return new UserProductivityDTO(userAnswer.getUser().getUsername(), 1L, 0L, "0:00", 0.0);
        }

        long totalSubmissions = 1L;
        long totalPouchesChecked = pac.getPouchesChecked();
        Duration duration = Duration.between(pac.getStartTime(), pac.getEndTime());
        long minutes = duration.toMinutes();
        String avgTimeDuration = String.format("%d:%02d", minutes / 60, minutes % 60);
        double avgPouchesPerHour = minutes > 0 ? (totalPouchesChecked * 60.0) / minutes : 0.0;

        return new UserProductivityDTO(
            userAnswer.getUser().getUsername(),
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
}
