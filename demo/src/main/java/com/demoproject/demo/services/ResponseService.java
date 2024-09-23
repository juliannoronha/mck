package com.demoproject.demo.services;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Service
public class ResponseService {

    private final UserAnswerRepository userAnswerRepository;
    private final UserProductivityService userProductivityService;
    private static final Logger logger = LoggerFactory.getLogger(UserProductivityService.class);

    public ResponseService(UserAnswerRepository userAnswerRepository, UserProductivityService userProductivityService) {
        this.userAnswerRepository = userAnswerRepository;
        this.userProductivityService = userProductivityService;
    }

    @Transactional
    public void submitUserAnswer(UserAnswer userAnswer, String username) {
        userAnswer.setName(username);
        userAnswer.setSubmissionDate(LocalDate.now());
        userAnswerRepository.save(userAnswer);
        logger.info("User answer submitted, notifying productivity update"); // Add this line
        userProductivityService.notifyProductivityUpdate();
    }

    public Page<UserAnswer> getAllResponses(Pageable pageable) {
        return userAnswerRepository.findAll(pageable);
    }

    @Transactional
    public void deleteResponse(Long id) {
        userAnswerRepository.deleteById(id);
    }
}