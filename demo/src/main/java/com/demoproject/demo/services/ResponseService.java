package com.demoproject.demo.services;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.UserAnswerRepository;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.repository.PacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List; // Change this import
import java.util.Optional; // Add this import
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.dao.TransientDataAccessException;

@Service
public class ResponseService {

    private final UserAnswerRepository userAnswerRepository;
    private final PacRepository pacRepository;
    private final UserProductivityService userProductivityService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    public ResponseService(UserAnswerRepository userAnswerRepository, PacRepository pacRepository,
                           UserProductivityService userProductivityService, UserRepository userRepository) {
        this.userAnswerRepository = userAnswerRepository;
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
    public void submitUserAnswer(UserAnswer userAnswer, Pac pac, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        userAnswer.setUser(user);
        userAnswer.setSubmissionDate(LocalDate.now());
        
        UserAnswer savedUserAnswer = userAnswerRepository.save(userAnswer);
        
        pac.setUserAnswer(savedUserAnswer);
        pac.setUser(user);  // Add this line to set the user on the Pac entity
        
        // Ensure all required fields are set
        if (pac.getStore() == null || pac.getStartTime() == null || pac.getEndTime() == null || pac.getPouchesChecked() == null) {
            throw new IllegalArgumentException("All required fields must be set for Pac");
        }
        
        pacRepository.save(pac);
        
        logger.info("Submitting user answer: username={}, store={}, pouchesChecked={}, startTime={}, endTime={}",
                    username, pac.getStore(), pac.getPouchesChecked(), pac.getStartTime(), pac.getEndTime());
        
        userProductivityService.notifyProductivityUpdate();
    }

    @Transactional
    public boolean deleteResponse(Long id) {
        Optional<UserAnswer> userAnswerOpt = userAnswerRepository.findById(id);
        if (userAnswerOpt.isPresent()) {
            userAnswerRepository.delete(userAnswerOpt.get());
            return true;
        }
        return false;
    }

    public Page<UserAnswer> getAllResponses(Pageable pageable) {
        return userAnswerRepository.findAll(pageable);
    }

    public Page<UserAnswer> getAllResponsesWithPac(Pageable pageable) {
        return userAnswerRepository.findAllWithPac(pageable);
    }

    public Page<UserAnswer> getAllResponsesWithPacSortedByDateDesc(Pageable pageable, String nameFilter) {
        if (nameFilter != null && !nameFilter.isEmpty()) {
            return userAnswerRepository.findAllWithPacAndNameFilter(pageable, nameFilter.toLowerCase());
        }
        return userAnswerRepository.findAllWithPac(pageable);
    }

    public Page<UserAnswer> getAllResponsesWithFilters(Pageable pageable, String nameFilter, String store, Integer month) {
        String lowercaseNameFilter = nameFilter != null && !nameFilter.isEmpty() ? nameFilter.toLowerCase() : null;
        String validStore = store != null && !store.isEmpty() ? store : null;

        long totalCount = userAnswerRepository.countAllWithFilters(lowercaseNameFilter, validStore, month);
        
        List<UserAnswer> content = userAnswerRepository.findAllWithFilters(pageable, lowercaseNameFilter, validStore, month);
        
        return new PageImpl<>(content, pageable, totalCount);
    }
}