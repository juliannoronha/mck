package com.demoproject.demo.services;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.PacRepository;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.pacmedproductivity.UserProductivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
public class PacService {
    private static final Logger logger = LoggerFactory.getLogger(PacService.class);
    private final PacRepository pacRepository;
    private final UserRepository userRepository;
    private final UserProductivityService userProductivityService;

    public PacService(PacRepository pacRepository,
                     UserRepository userRepository,
                     UserProductivityService userProductivityService) {
        this.pacRepository = pacRepository;
        this.userRepository = userRepository;
        this.userProductivityService = userProductivityService;
    }

    /**
     * Submits a new PAC entry with retry capability.
     */
    @Retryable(
        value = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitPac(Pac pac, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        pac.setUser(user);
        
        validatePac(pac);
        pacRepository.save(pac);
        
        logger.info("Submitting pac: username={}, store={}, pouchesChecked={}, startTime={}, endTime={}",
                    username, pac.getStore(), pac.getPouchesChecked(), pac.getStartTime(), pac.getEndTime());
        
        userProductivityService.notifyProductivityUpdate();
    }

    private void validatePac(Pac pac) {
        if (pac.getStore() == null || pac.getStartTime() == null || 
            pac.getEndTime() == null || pac.getPouchesChecked() == null) {
            throw new IllegalArgumentException("All required fields must be set for Pac");
        }
    }

    /**
     * Retrieves all PACs with filters and pagination.
     */
    public Page<Pac> getAllPacsWithFilters(Pageable pageable, String nameFilter, String store, Integer month) {
        String lowercaseNameFilter = nameFilter != null && !nameFilter.isEmpty() ? nameFilter.toLowerCase() : null;
        String validStore = store != null && !store.isEmpty() ? store : null;
        return pacRepository.findAllWithFilters(pageable, lowercaseNameFilter, validStore, month);
    }

    /**
     * Retrieves all PACs with pagination.
     */
    public Page<Pac> getAllPacs(Pageable pageable) {
        return pacRepository.findAll(pageable);
    }

    /**
     * Deletes a PAC by its ID.
     */
    @Transactional
    public boolean deletePac(Long id) {
        Optional<Pac> pacOpt = pacRepository.findById(id);
        if (pacOpt.isPresent()) {
            pacRepository.delete(pacOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Retrieves a PAC by its ID.
     */
    public Pac getPacById(Long id) {
        return pacRepository.findById(id).orElse(null);
    }
}