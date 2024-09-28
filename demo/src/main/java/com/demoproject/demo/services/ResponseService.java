package com.demoproject.demo.services;

import com.demoproject.demo.entity.User;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.repository.PacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.annotation.Propagation;

/**
 * Service class responsible for managing user responses and PAC (Pouch Accuracy Check) submissions.
 * This class handles operations such as submitting user answers, deleting responses, and retrieving responses with filters.
 */
@Service
public class ResponseService {

    private final PacRepository pacRepository;
    private final UserProductivityService userProductivityService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    /**
     * Constructs a new ResponseService with necessary dependencies.
     *
     * @param pacRepository Repository for PAC data operations
     * @param userProductivityService Service for managing user productivity
     * @param userRepository Repository for user data operations
     */
    public ResponseService(PacRepository pacRepository,
                           UserProductivityService userProductivityService, 
                           UserRepository userRepository) {
        this.pacRepository = pacRepository;
        this.userProductivityService = userProductivityService;
        this.userRepository = userRepository;
    }

    /**
     * Submits a user answer (PAC) with retry capability for transient data access issues.
     *
     * @param pac The PAC object to be submitted
     * @param username The username of the user submitting the PAC
     * @throws RuntimeException if the user is not found
     * @throws IllegalArgumentException if required PAC fields are not set
     */
    @Retryable(
        value = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitUserAnswer(Pac pac, String username) {
        // Retrieve user or throw exception if not found
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        pac.setUser(user);
        
        // Validate required PAC fields
        if (pac.getStore() == null || pac.getStartTime() == null || pac.getEndTime() == null || pac.getPouchesChecked() == null) {
            throw new IllegalArgumentException("All required fields must be set for Pac");
        }
        
        // Save the PAC
        pacRepository.save(pac);
        
        // Log the PAC submission
        logger.info("Submitting pac: username={}, store={}, pouchesChecked={}, startTime={}, endTime={}",
                    username, pac.getStore(), pac.getPouchesChecked(), pac.getStartTime(), pac.getEndTime());
        
        // Notify productivity update
        userProductivityService.notifyProductivityUpdate();
    }

    /**
     * Deletes a response (PAC) by its ID.
     *
     * @param id The ID of the PAC to be deleted
     * @return true if the PAC was found and deleted, false otherwise
     */
    @Transactional
    public boolean deleteResponse(Long id) {
        Optional<Pac> pacOpt = pacRepository.findById(id);
        if (pacOpt.isPresent()) {
            pacRepository.delete(pacOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Retrieves all responses (PACs) with pagination.
     *
     * @param pageable Pagination information
     * @return A Page of PAC objects
     */
    public Page<Pac> getAllResponses(Pageable pageable) {
        return pacRepository.findAll(pageable);
    }

    /**
     * Retrieves all responses (PACs) with filters and pagination.
     *
     * @param pageable Pagination information
     * @param nameFilter Filter for user name (case-insensitive)
     * @param store Filter for store
     * @param month Filter for month
     * @return A Page of filtered PAC objects
     */
    public Page<Pac> getAllResponsesWithFilters(Pageable pageable, String nameFilter, String store, Integer month) {
        String lowercaseNameFilter = nameFilter != null && !nameFilter.isEmpty() ? nameFilter.toLowerCase() : null;
        String validStore = store != null && !store.isEmpty() ? store : null;

        return pacRepository.findAllWithFilters(pageable, lowercaseNameFilter, validStore, month);
    }

    // TODO: Implement method to retrieve user-specific PAC statistics
    // TODO: Add functionality to export PAC data in various formats (CSV, PDF)
    // TODO: Implement a method to calculate and update user efficiency based on PAC submissions
}