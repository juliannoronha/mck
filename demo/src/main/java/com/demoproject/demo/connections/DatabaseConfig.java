/* ==========================================================================
 * Database Configuration Module
 *
 * PURPOSE: Configures and manages HikariCP database connection pool
 * DEPENDENCIES: HikariCP, Metrics, Spring Framework
 * SCOPE: Application-wide database connectivity
 * 
 * SECURITY CONSIDERATIONS:
 * - Credentials loaded from external configuration
 * - Connection pool limits prevent resource exhaustion
 * - Leak detection enabled
 * ========================================================================== */

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
    
    /* .... Configuration Properties .... */
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    /* --------------------------------------------------------------------------
     * HikariCP Configuration
     * 
     * @returns Configured HikariConfig instance
     * 
     * POOL SETTINGS:
     * - Maximum 10 connections to prevent resource exhaustion
     * - Minimum 2 idle connections for performance
     * - 10 minute idle timeout for connection reuse
     * - 20 second connection timeout for fast failure
     * 
     * MONITORING:
     * - Leak detection at 30 seconds
     * - Metrics registration enabled
     * - MBeans exposed for monitoring
     * -------------------------------------------------------------------------- */
    @Bean
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        
        /* Connection Pool Settings */
        config.setMaximumPoolSize(10);          
        config.setMinimumIdle(2);               
        config.setIdleTimeout(600000);          
        config.setConnectionTimeout(20000);      
        config.setMaxLifetime(1200000);         
        config.setLeakDetectionThreshold(30000); 
        
        /* Connection Testing & Performance */
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        /* Monitoring Configuration */
        config.addDataSourceProperty("registerMbeans", "true");
        config.setPoolName("MainHikariPool");
        config.setMetricRegistry(new MetricRegistry());
        
        /* Performance Optimizations */
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        /* Connection Cleanup Settings */
        config.setAutoCommit(true);
        config.setInitializationFailTimeout(1);
        config.setValidationTimeout(5000);
        config.addDataSourceProperty("useDisposableConnectionFacade", "true");
        config.addDataSourceProperty("closeConnectionWatch", "true");
        
        return config;
    }
    
    /* --------------------------------------------------------------------------
     * Primary DataSource Configuration
     * 
     * @returns HikariDataSource configured as primary application data source
     * @note Marked as @Primary for auto-injection when multiple sources exist
     * -------------------------------------------------------------------------- */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    /* @todo [MONITOR] Add connection pool metrics logging
     * @todo [SECURITY] Implement connection encryption
     * @todo [PERF] Tune pool sizes based on metrics
     * @todo [RESILIENCE] Add connection retry logic
     */
}