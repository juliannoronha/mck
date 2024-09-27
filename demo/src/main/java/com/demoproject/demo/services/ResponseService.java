package com.demoproject.demo.services;

import com.demoproject.demo.entity.User;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.repository.PacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.TransientDataAccessException;

@Service
public class ResponseService {

    private final PacRepository pacRepository;
    private final UserProductivityService userProductivityService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    public ResponseService(PacRepository pacRepository,
                           UserProductivityService userProductivityService, 
                           UserRepository userRepository) {
        this.pacRepository = pacRepository;
        this.userProductivityService = userProductivityService;
        this.userRepository = userRepository;
    }

    @Retryable(
        value = {TransientDataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Transactional
    public void submitUserAnswer(Pac pac, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        pac.setUser(user);
        
        if (pac.getStore() == null || pac.getStartTime() == null || pac.getEndTime() == null || pac.getPouchesChecked() == null) {
            throw new IllegalArgumentException("All required fields must be set for Pac");
        }
        
        pacRepository.save(pac);
        
        logger.info("Submitting pac: username={}, store={}, pouchesChecked={}, startTime={}, endTime={}",
                    username, pac.getStore(), pac.getPouchesChecked(), pac.getStartTime(), pac.getEndTime());
        
        userProductivityService.notifyProductivityUpdate();
    }

    @Transactional
    public boolean deleteResponse(Long id) {
        Optional<Pac> pacOpt = pacRepository.findById(id);
        if (pacOpt.isPresent()) {
            pacRepository.delete(pacOpt.get());
            return true;
        }
        return false;
    }

    public Page<Pac> getAllResponses(Pageable pageable) {
        return pacRepository.findAll(pageable);
    }

    public Page<Pac> getAllResponsesWithFilters(Pageable pageable, String nameFilter, String store, Integer month) {
        String lowercaseNameFilter = nameFilter != null && !nameFilter.isEmpty() ? nameFilter.toLowerCase() : null;
        String validStore = store != null && !store.isEmpty() ? store : null;

        return pacRepository.findAllWithFilters(pageable, lowercaseNameFilter, validStore, month);
    }
}