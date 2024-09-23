package com.demoproject.demo.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataMigrationService {

    private final JdbcTemplate jdbcTemplate;

    public DataMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Your migration methods here
}