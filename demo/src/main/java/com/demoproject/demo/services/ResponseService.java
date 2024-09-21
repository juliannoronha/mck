package com.demoproject.demo.services;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Service
public class ResponseService {

    private final UserAnswerRepository userAnswerRepository;

    public ResponseService(UserAnswerRepository userAnswerRepository) {
        this.userAnswerRepository = userAnswerRepository;
    }

    @Transactional
    public void submitUserAnswer(UserAnswer userAnswer, String username) {
        userAnswer.setName(username);
        userAnswer.setSubmissionDate(LocalDate.now());
        userAnswerRepository.save(userAnswer);
    }

    public Page<UserAnswer> getAllResponses(Pageable pageable) {
        return userAnswerRepository.findAll(pageable);
    }

    @Transactional
    public void deleteResponse(Long id) {
        userAnswerRepository.deleteById(id);
    }
}