package com.demoproject.demo.services;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.entity.User;
import com.demoproject.demo.repository.UserAnswerRepository;
import com.demoproject.demo.repository.UserRepository; // Add this
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@Service
public class ResponseService {

    private final UserAnswerRepository userAnswerRepository;
    private final UserProductivityService userProductivityService;
    private final UserRepository userRepository; // Add this
    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    public ResponseService(UserAnswerRepository userAnswerRepository, UserProductivityService userProductivityService, UserRepository userRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.userProductivityService = userProductivityService;
        this.userRepository = userRepository; // Add this
    }

    @Transactional
    public void submitUserAnswer(UserAnswer userAnswer, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        userAnswer.setName(username);
        userAnswer.setUser(user); // Set the user
        userAnswer.setSubmissionDate(LocalDate.now());
        
        logger.info("Submitting user answer: username={}, store={}, pouchesChecked={}, startTime={}, endTime={}",
                    username, userAnswer.getStore(), userAnswer.getPouchesChecked(), userAnswer.getStartTime(), userAnswer.getEndTime());
        
        userAnswerRepository.save(userAnswer);
        userProductivityService.notifyProductivityUpdate();
    }

    @Transactional
    public void deleteResponse(Long id) {
        userAnswerRepository.deleteById(id);
        userProductivityService.notifyProductivityUpdate();
    }

    public Page<UserAnswer> getAllResponses(Pageable pageable) {
        return userAnswerRepository.findAll(pageable);
    }
}