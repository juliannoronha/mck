package com.demoproject.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure default cache settings
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(100)
            .recordStats());
        
        // Define specific caches
        cacheManager.setCacheNames(Arrays.asList(
            "allUserProductivity",
            "userProductivity",
            "overallProductivity"
        ));
        
        return cacheManager;
    }
    
    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .initialCapacity(10)
            .maximumSize(100)
            .recordStats();
    }
}
