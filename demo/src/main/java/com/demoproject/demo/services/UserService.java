package com.demoproject.demo.services;

import com.demoproject.demo.dto.UserDTO;
import com.demoproject.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import com.demoproject.demo.dto.UserProductivityDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.demoproject.demo.entity.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing user-related operations.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRegistrationService registrationService;
    private final UserDeletionService deletionService;
    private final UserProductivityService productivityService;
    private final PasswordManagementService passwordService;
    private final UserRepository userRepository;

    public UserService(UserRegistrationService registrationService,
                       UserDeletionService deletionService,
                       UserProductivityService productivityService,
                       PasswordManagementService passwordService,
                       UserRepository userRepository) {
        this.registrationService = registrationService;
        this.deletionService = deletionService;
        this.productivityService = productivityService;
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    // Delegate methods to appropriate services
    /**
     * Registers a new user.
     *
     * @param userDTO the user data transfer object
     */
    public void registerNewUser(UserDTO userDTO) {
        logger.info("Registering new user: {}", userDTO.getUsername());
        registrationService.registerNewUser(userDTO);
    }

    /**
     * Deletes a user by username.
     *
     * @param username the username of the user to delete
     */
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Retrieves productivity data for a specific user.
     *
     * @param username the username of the user
     * @return a map containing productivity data
     */
    public Map<String, Object> getUserProductivity(String username) {
        logger.info("Retrieving productivity data for user: {}", username);
        return productivityService.getUserProductivity(username);
    }

    /**
     * Retrieves productivity data for all users.
     *
     * @return a list of UserProductivityDTO objects
     */
    public List<UserProductivityDTO> getAllUserProductivity() {
        logger.info("Retrieving productivity data for all users.");
        return productivityService.getAllUserProductivity(0, Integer.MAX_VALUE).getContent();
    }

    /**
     * Retrieves productivity data for all users, paginated.
     *
     * @param page the page number
     * @param size the size of each page
     * @return a page of UserProductivityDTO objects
     */
    public Page<UserProductivityDTO> getAllUserProductivity(int page, int size) {
        logger.info("Retrieving productivity data for all users. Page: {}, Size: {}", page, size);
        return productivityService.getAllUserProductivity(page, size);
    }

    /**
     * Changes the password for a user.
     *
     * @param username the username of the user
     * @param newPassword the new password
     */
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for user: {}", username);
        passwordService.changePassword(username, newPassword);
    }

    /**
     * Retrieves all users, paginated.
     *
     * @param pageable the pagination parameters
     * @return a page of User objects
     */
    public Page<User> getAllUsers(Pageable pageable) {
        logger.info("Retrieving all users. Pageable: {}", pageable);
        return userRepository.findAll(pageable);
    }
}