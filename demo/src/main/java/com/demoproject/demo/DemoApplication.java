package com.demoproject.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for the Demo project.
 * This class serves as the entry point for the Spring Boot application.
 */
@SpringBootApplication
@EntityScan("com.demoproject.demo.entity")
@EnableJpaRepositories("com.demoproject.demo.repository")
public class DemoApplication {

    /**
     * The main method which serves as the entry point for the application.
     * It uses SpringApplication to bootstrap and launch the Spring application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}