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
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;
import com.demoproject.demo.dto.UserProductivityDTO;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        List<UserProductivityQueryDTO> results = userAnswerRepository.getUserProductivitySummary(pageable);
        return results.stream().map(result -> {
            String username = result.getUsername();
            Long totalSubmissions = result.getTotalSubmissions();
            Double avgPouchesChecked = result.getAvgPouchesChecked();
            Long totalPouchesChecked = result.getTotalPouchesChecked();
            
            String avgTimeDuration = calculateAverageDuration(username);
            
            return new UserProductivityDTO(
                username,
                totalSubmissions.intValue(),
                avgTimeDuration,
                calculateAveragePouchesPerHour(avgPouchesChecked, avgTimeDuration),
                totalPouchesChecked.intValue()
            );
        }).collect(Collectors.toList());
    }

    private String calculateAverageDuration(String username) {
        List<UserAnswer> userAnswers = userAnswerRepository.findByName(username);
        if (userAnswers.isEmpty()) {
            return "0h 0m";
        }

        long totalMinutes = 0;
        for (UserAnswer answer : userAnswers) {
            LocalTime startTime = answer.getStartTime();
            LocalTime endTime = answer.getEndTime();
            Duration duration = Duration.between(startTime, endTime);
            totalMinutes += duration.toMinutes();
        }

        double averageMinutes = (double) totalMinutes / userAnswers.size();
        return formatDuration(averageMinutes);
    }

    private double calculateAveragePouchesPerHour(Double avgPouchesChecked, String avgTimeDuration) {
        // Parse the time duration string
        String[] parts = avgTimeDuration.split(" ");
        double hours = 0;
        for (String part : parts) {
            if (part.endsWith("h")) {
                hours += Double.parseDouble(part.replace("h", ""));
            } else if (part.endsWith("m")) {
                hours += Double.parseDouble(part.replace("m", "")) / 60;
            }
        }

        // Avoid division by zero
        if (hours == 0) {
            return 0;
        }

        // Calculate pouches per hour
        return avgPouchesChecked / hours;
    }

    private String formatDuration(double minutes) {
        long hours = (long) (minutes / 60);
        long remainingMinutes = (long) (minutes % 60);
        return String.format("%dh %dm", hours, remainingMinutes);
    }

    public UserProductivityDTO getOverallProductivity() {
        Long totalSubmissions = userAnswerRepository.count();
        Double avgPouchesPerHour = calculateOverallAveragePouchesPerHour();
        String avgTimeDuration = calculateOverallAverageDuration();
        Long totalPouchesChecked = userAnswerRepository.getTotalPouchesChecked();
        
        return new UserProductivityDTO("Overall", totalSubmissions.intValue(), avgTimeDuration, avgPouchesPerHour, totalPouchesChecked.intValue());
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