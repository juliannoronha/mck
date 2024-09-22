package com.demoproject.demo.services;

import com.demoproject.demo.dto.UserProductivityQueryDTO;
import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import com.demoproject.demo.dto.UserProductivityDTO;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.temporal.ChronoUnit; // Add this import

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserAnswerRepository userAnswerRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constructor for UserService.
     * @param passwordEncoder Encoder for password hashing.
     * @param userRepository Repository for user data operations.
     * @param userAnswerRepository Repository for user answer data operations.
     */
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, UserAnswerRepository userAnswerRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userAnswerRepository = userAnswerRepository;
    }

    /**
     * Registers a new user.
     * @param userDTO Data Transfer Object containing user information.
     * @throws DataIntegrityViolationException if the username already exists.
     */
    @Transactional
    public void registerNewUser(UserDTO userDTO) {
        logger.info("Attempting to register new user with username: {} and role: {}", userDTO.getUsername(), userDTO.getRole());
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        try {
            User.Role role = User.Role.valueOf(userDTO.getRole().toUpperCase());
            user.setRole(role);
            logger.info("Role set successfully: {}", role);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role: {}", userDTO.getRole());
            throw new IllegalArgumentException("Invalid role selected: " + userDTO.getRole());
        }
        
        try {
            User savedUser = userRepository.save(user);
            logger.info("Successfully registered new user: {} with role: {}", savedUser.getUsername(), savedUser.getRole());
        } catch (DataIntegrityViolationException e) {
            logger.error("Failed to register user. Database error: {}", e.getMessage());
            throw new DataIntegrityViolationException("Failed to register user: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while registering user: {}", e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
    }

    /**
     * Deletes a user by username.
     * @param username The username of the user to delete.
     * @throws IllegalArgumentException if attempting to delete the protected user.
     * @throws RuntimeException if the user is not found.
     */
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if ("admin".equals(username)) {
            throw new IllegalArgumentException("Cannot delete protected user");
        }
        
        userRepository.delete(user);
        logger.info("User deleted: {}", username);
    }

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users from the database", users.size());
        return users;
    }

    /**
     * Checks if a user exists by username.
     * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Deletes a user by username.
     * @param username The username of the user to delete.
     * @throws RuntimeException if the user is not found.
     */
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Retrieves user productivity data.
     * @param username The username of the user to retrieve productivity data for.
     * @return A map containing productivity data.
     */
    public Map<String, Object> getUserProductivity(String username) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByName(username);
        
        long totalSubmissions = userAnswers.size();
        
        double avgTimeDurationMinutes = userAnswers.stream()
            .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
            .average()
            .orElse(0);
        
        String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
        
        double avgPouchesPerHour = userAnswers.stream()
            .mapToDouble(answer -> {
                double hours = Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0;
                return answer.getPouchesChecked() / hours;
            })
            .average()
            .orElse(0);

        Map<String, Object> productivity = new HashMap<>();
        productivity.put("username", username);
        productivity.put("totalSubmissions", totalSubmissions);
        productivity.put("avgTimeDuration", avgTimeDuration);
        productivity.put("avgPouchesPerHour", avgPouchesPerHour);

        return productivity;
    }

    public String calculateAvgTimeDuration(List<UserAnswer> userAnswers) {
        double avgHours = userAnswers.stream()
                .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
                .average()
                .orElse(0) / 60.0;
        int wholeHours = (int) avgHours;
        int minutes = (int) ((avgHours - wholeHours) * 60);
        return String.format("%dh %dm", wholeHours, minutes);
    }

    public double calculateAvgPouchesPerHour(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .mapToDouble(answer -> {
                    double hours = Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0;
                    return answer.getPouchesChecked() / hours;
                })
                .average()
                .orElse(0);
    }

    public List<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UserAnswerRepository.UserProductivityQueryProjection> results = userAnswerRepository.getUserProductivitySummary(pageable);
        return results.stream().map(result -> {
            String username = result.getUsername();
            Long totalSubmissions = result.getTotalSubmissions();
            Long totalPouchesChecked = result.getTotalPouchesChecked();
            Long totalMinutes = result.getTotalMinutes();
            
            Double avgPouchesChecked = totalPouchesChecked.doubleValue() / totalSubmissions;
            Double avgTimeDurationMinutes = totalMinutes.doubleValue() / totalSubmissions;
            String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
            Double avgPouchesPerHour = totalPouchesChecked.doubleValue() / (totalMinutes.doubleValue() / 60);
            
            return new UserProductivityDTO(
                username,
                totalSubmissions.intValue(),
                avgTimeDuration,
                avgPouchesPerHour,
                totalPouchesChecked.intValue(),
                avgPouchesChecked
            );
        }).collect(Collectors.toList());
    }

    private String formatDuration(Double minutes) {
        long hours = (long) (minutes / 60);
        long remainingMinutes = (long) (minutes % 60);
        return String.format("%dh %dm", hours, remainingMinutes);
    }

    public UserProductivityDTO getOverallProductivity() {
        List<UserAnswer> allAnswers = userAnswerRepository.findAll();
        
        long totalSubmissions = allAnswers.size();
        long totalPouchesChecked = allAnswers.stream().mapToLong(UserAnswer::getPouchesChecked).sum();
        long totalMinutes = allAnswers.stream()
            .mapToLong(answer -> ChronoUnit.MINUTES.between(answer.getStartTime(), answer.getEndTime()))
            .sum();

        Double avgPouchesChecked = totalSubmissions > 0 ? (double) totalPouchesChecked / totalSubmissions : 0.0;
        Double avgTimeDurationMinutes = totalSubmissions > 0 ? (double) totalMinutes / totalSubmissions : 0.0;
        String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
        Double avgPouchesPerHour = totalMinutes > 0 ? (double) totalPouchesChecked / (totalMinutes / 60.0) : 0.0;

        return new UserProductivityDTO(
            "Overall",
            (int) totalSubmissions,
            avgTimeDuration,
            avgPouchesPerHour,
            (int) totalPouchesChecked,
            avgPouchesChecked
        );
    }

    private String calculateOverallAverageDuration() {
        List<UserAnswer> allAnswers = userAnswerRepository.findAll();
        if (allAnswers.isEmpty()) {
            return "0h 0m";
        }

        long totalMinutes = allAnswers.stream()
            .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
            .sum();

        double averageMinutes = (double) totalMinutes / allAnswers.size();
        return formatDuration(averageMinutes);
    }

    private double calculateOverallAveragePouchesPerHour() {
        List<UserAnswer> allAnswers = userAnswerRepository.findAll();
        if (allAnswers.isEmpty()) {
            return 0;
        }

        double totalHours = allAnswers.stream()
            .mapToDouble(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0)
            .sum();

        long totalPouches = allAnswers.stream()
            .mapToLong(UserAnswer::getPouchesChecked)
            .sum();

        return totalHours > 0 ? totalPouches / totalHours : 0;
    }
}