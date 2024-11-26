package com.demoproject.demo.connections;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.codahale.metrics.MetricRegistry;
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
        config.setMaximumPoolSize(10);          // Reduced to prevent resource exhaustion
        config.setMinimumIdle(2);               // Reduced to optimize resource usage
        config.setIdleTimeout(600000);          // Increased to 10 minutes for better connection reuse
        config.setConnectionTimeout(20000);      // Reduced to fail fast
        config.setMaxLifetime(1200000);         // Reduced to 20 minutes to prevent stale connections
        config.setLeakDetectionThreshold(30000); // Reduced to 30 seconds for faster leak detection
        
        // Connection testing and performance settings
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Added health check and monitoring properties
        config.addDataSourceProperty("registerMbeans", "true");
        config.setPoolName("MainHikariPool");
        config.setMetricRegistry(new MetricRegistry()); // Requires metrics dependency
        
        // Enhanced performance properties
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return config;
    }
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }
}