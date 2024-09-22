package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalTime;

@Service
public class UserProductivityService {
    private final UserAnswerRepository userAnswerRepository;

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

    public List<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserAnswer> userAnswersPage = userAnswerRepository.findAll(pageable);

        return userAnswersPage.getContent().stream()
            .collect(Collectors.groupingBy(UserAnswer::getName))
            .entrySet().stream()
            .map(entry -> {
                String username = entry.getKey();
                List<UserAnswer> userAnswers = entry.getValue();
                
                int totalPouches = userAnswers.stream().mapToInt(UserAnswer::getPouchesChecked).sum();
                long totalMinutes = userAnswers.stream()
                    .mapToLong(answer -> {
                        LocalTime startTime = answer.getStartTime();
                        LocalTime endTime = answer.getEndTime();
                        return Duration.between(startTime, endTime).toMinutes();
                    })
                    .sum();

                double avgPouchesPerHour = totalMinutes > 0 ? (totalPouches * 60.0) / totalMinutes : 0;
                String avgTimeDuration = String.format("%d:%02d", totalMinutes / 60, totalMinutes % 60);
                double avgPouchesChecked = userAnswers.size() > 0 ? (double) totalPouches / userAnswers.size() : 0;

                return new UserProductivityDTO(
                    username,
                    userAnswers.size(),
                    avgTimeDuration,
                    avgPouchesPerHour,
                    totalPouches,
                    avgPouchesChecked
                );
            })
            .collect(Collectors.toList());
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
            totalSubmissions,
            avgTimeDuration,
            avgPouchesPerHour,
            totalPouchesChecked,
            avgPouchesChecked
        );
    }
}
