package com.demoproject.demo.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class NbaAPIConfig {
    
    @Value("${nba.api.key}")
    private String apiKey;
    
    @Value("${nba.api.host}")
    private String apiHost;
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    @Bean
    public String apiKey() {
        return apiKey;
    }
    
    @Bean
    public String apiHost() {
        return apiHost;
    }
}
