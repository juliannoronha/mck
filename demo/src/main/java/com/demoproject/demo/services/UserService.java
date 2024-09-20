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
import java.util.ArrayList;

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
        logger.info("Attempting to register new user: {}", userDTO.getUsername());
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Username already exists: {}", userDTO.getUsername());
            throw new DataIntegrityViolationException("Username already exists");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());

        
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(User.Role.valueOf(userDTO.getRole().name()));
        logger.info("Saving user to database: {}", user);
        User savedUser = userRepository.save(user);
        userRepository.flush();
        logger.info("User registered successfully: {}", savedUser);
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
        
        // Check if user is a protected user (e.g., admin)
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
        
        Map<String, Object> productivity = new HashMap<>();
        productivity.put("totalSubmissions", userAnswers.size());
        productivity.put("avgTimeDuration", calculateAvgTimeDuration(userAnswers));
        productivity.put("avgPouchesPerHour", calculateAvgPouchesPerHour(userAnswers));
        
        return productivity;
    }

    private String calculateAvgTimeDuration(List<UserAnswer> userAnswers) {
        double avgHours = userAnswers.stream()
                .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
                .average()
                .orElse(0) / 60.0;
        int wholeHours = (int) avgHours;
        int minutes = (int) ((avgHours - wholeHours) * 60);
        return String.format("%dh %dm", wholeHours, minutes);
    }

    private double calculateAvgPouchesPerHour(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .mapToDouble(answer -> {
                    double hours = Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0;
                    return answer.getPouchesChecked() / hours;
                })
                .average()
                .orElse(0);
    }

    public List<UserProductivityDTO> getAllUserProductivity() {
        List<Object[]> results = userAnswerRepository.getUserProductivityData();
        List<UserProductivityDTO> productivityList = new ArrayList<>();

        for (Object[] result : results) {
            String username = (String) result[0];
            Long totalSubmissions = (Long) result[1];
            Double avgDurationMinutes = (Double) result[2];
            Double avgPouchesPerHour = (Double) result[3];

            String formattedDuration = formatDuration(avgDurationMinutes);

            productivityList.add(new UserProductivityDTO(
                username,
                totalSubmissions.intValue(),
                formattedDuration,
                avgPouchesPerHour
            ));
        }

        return productivityList;
    }

    private String formatDuration(Double minutes) {
        if (minutes == null) return "N/A";
        long hours = (long) (minutes / 60);
        long remainingMinutes = (long) (minutes % 60);
        return String.format("%dh %dm", hours, remainingMinutes);
    }

    public UserProductivityDTO getOverallProductivity() {
        long totalSubmissions = userAnswerRepository.count();
        
        List<UserAnswer> allAnswers = userAnswerRepository.findAll();
        
        double avgTimeDurationMinutes = allAnswers.stream()
            .mapToLong(answer -> Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes())
            .average()
            .orElse(0);
        
        String avgTimeDuration = formatDuration(avgTimeDurationMinutes);
        
        double avgPouchesPerHour = allAnswers.stream()
            .mapToDouble(answer -> {
                double hours = Duration.between(answer.getStartTime(), answer.getEndTime()).toMinutes() / 60.0;
                return answer.getPouchesChecked() / hours;
            })
            .average()
            .orElse(0);

        logger.info("Calculated overall productivity: submissions={}, avgTime={}, avgPouches={}", 
                totalSubmissions, avgTimeDuration, avgPouchesPerHour);

        return new UserProductivityDTO("Overall", (int) totalSubmissions, avgTimeDuration, avgPouchesPerHour);
    }

    private String formatDuration(double minutes) {
        long hours = (long) (minutes / 60);
        long remainingMinutes = (long) (minutes % 60);
        return String.format("%dh %dm", hours, remainingMinutes);
    }
}