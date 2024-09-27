package com.demoproject.demo.repository;

import com.demoproject.demo.entity.UserAnswer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    List<UserAnswer> findByUser_Username(String username);

    @Query("SELECT ua FROM UserAnswer ua WHERE ua.user.username = :username")
    List<UserAnswer> findAllByUsername(@Param("username") String username);

    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.user.username = :username")
    long countByUsername(@Param("username") String username);

    @Query("SELECT ua FROM UserAnswer ua")
    List<UserAnswer> findAllReadOnly();
}

