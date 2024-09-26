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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

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

    @Transactional
    public void submitUserAnswer(UserAnswer userAnswer, Pac pac, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        userAnswer.setUser(user);
        userAnswer.setSubmissionDate(LocalDate.now());
        
        UserAnswer savedUserAnswer = userAnswerRepository.save(userAnswer);
        
        pac.setUserAnswer(savedUserAnswer);
        
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
    public void deleteResponse(Long id) {
        UserAnswer userAnswer = userAnswerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Response not found with id: " + id));
        
        // The cascading delete will take care of deleting the related Pac entity
        userAnswerRepository.delete(userAnswer);
    }

    public Page<UserAnswer> getAllResponses(Pageable pageable) {
        return userAnswerRepository.findAll(pageable);
    }

    public Page<UserAnswer> getAllResponsesWithPac(Pageable pageable) {
        return userAnswerRepository.findAllWithPac(pageable);
    }
}