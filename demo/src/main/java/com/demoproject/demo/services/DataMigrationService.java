package com.demoproject.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Service
public class DataMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void migrateData() throws Exception {
        DataSource dataSource = jdbcTemplate.getDataSource();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Migrate users table
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                jdbcTemplate.update("INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)",
                        rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"));
            }
            
            // Add more table migrations as needed
        }
    }
}