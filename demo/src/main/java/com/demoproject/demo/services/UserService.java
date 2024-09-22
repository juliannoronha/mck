package com.demoproject.demo.services;

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
import java.time.temporal.ChronoUnit;

import java.util.Collections;

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
        if (userDTO == null) {
            logger.error("Attempt to register null UserDTO");
            throw new IllegalArgumentException("UserDTO cannot be null");
        }
        
        logger.info("Attempting to register new user with username: {} and role: {}", userDTO.getUsername(), userDTO.getRole());
        
        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            logger.error("Attempt to register user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            logger.error("Attempt to register user with null or empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        if (userDTO.getRole() == null || userDTO.getRole().trim().isEmpty()) {
            logger.error("Attempt to register user with null or empty role");
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
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
        if (username == null || username.trim().isEmpty()) {
            logger.error("Attempt to delete user with null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Attempt to delete non-existent user: {}", username);
                return new IllegalArgumentException("User not found: " + username);
            });
        
        if ("admin".equals(username)) {
            logger.error("Attempt to delete protected user: {}", username);
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
        if (username == null || username.trim().isEmpty()) {
            logger.error("Attempt to get productivity for null or empty username");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        List<UserAnswer> userAnswers = userAnswerRepository.findByName(username);
        
        if (userAnswers.isEmpty()) {
            logger.warn("No user answers found for username: {}", username);
            return Collections.emptyMap();
        }
        
        long totalSubmissions = userAnswers.size();
        
        double avgTimeDurationMinutes = userAnswers.stream()
            .filter(answer -> answer.getStartTime() != null && answer.getEndTime() != null)
            .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
            .average()
            .orElse(0);
        
        String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
        
        double avgPouchesPerHour = userAnswers.stream()
            .filter(answer -> answer.getStartTime() != null && answer.getEndTime() != null)
            .mapToDouble(answer -> {
                double hours = Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0;
                return hours > 0 ? answer.getPouchesChecked() / hours : 0;
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

    public List<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        if (page < 0 || size <= 0) {
            logger.error("Invalid page or size parameters: page={}, size={}", page, size);
            throw new IllegalArgumentException("Invalid page or size parameters");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        List<UserAnswerRepository.UserProductivityQueryProjection> results = userAnswerRepository.getUserProductivitySummary(pageable);
        
        return results.stream()
            .filter(result -> result != null && result.getUsername() != null)
            .map(result -> {
                String username = result.getUsername();
                Long totalSubmissions = result.getTotalSubmissions() != null ? result.getTotalSubmissions() : 0L;
                Long totalPouchesChecked = result.getTotalPouchesChecked() != null ? result.getTotalPouchesChecked() : 0L;
                Long totalMinutes = result.getTotalMinutes() != null ? result.getTotalMinutes() : 0L;
                
                Double avgPouchesChecked = totalSubmissions > 0 ? totalPouchesChecked.doubleValue() / totalSubmissions : 0.0;
                Double avgTimeDurationMinutes = totalSubmissions > 0 ? totalMinutes.doubleValue() / totalSubmissions : 0.0;
                String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
                Double avgPouchesPerHour = totalMinutes > 0 ? totalPouchesChecked.doubleValue() / (totalMinutes.doubleValue() / 60) : 0.0;
                
                return new UserProductivityDTO(
                    username,
                    totalSubmissions.intValue(),
                    avgTimeDuration,
                    avgPouchesPerHour,
                    totalPouchesChecked.intValue(),
                    avgPouchesChecked
                );
            })
            .collect(Collectors.toList());
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
}