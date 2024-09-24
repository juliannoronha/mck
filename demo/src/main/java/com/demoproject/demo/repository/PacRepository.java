package com.demoproject.demo.repository;

import com.demoproject.demo.entity.Pac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacRepository extends JpaRepository<Pac, Long> {
    List<Pac> findByUserAnswer_User_Username(String username);
}