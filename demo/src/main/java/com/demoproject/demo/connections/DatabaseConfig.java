package com.demoproject.demo.connections;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    @Bean
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        
        // Optimized connection pool settings
        config.setMaximumPoolSize(20);          // Increased from 10
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);          // 5 minutes
        config.setConnectionTimeout(30000);      // Increased from 20000
        config.setMaxLifetime(1800000);         // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setValidationTimeout(5000);       // 5 seconds
        
        // Connection testing and performance settings
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return config;
    }
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }
}