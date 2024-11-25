package com.demoproject.demo.config;

import com.demoproject.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * SecurityConfig: Central configuration for Spring Security settings.
 * 
 * This class orchestrates the security setup for the application, including:
 * - Authentication mechanisms
 * - Authorization rules
 * - Password encoding
 * - Session management
 * - Custom error handling
 *
 * Key features:
 * - Uses BCrypt for password hashing
 * - Implements role-based access control
 * - Configures secure channel requirements
 * - Sets up custom login and logout behavior
 * - Manages session fixation protection
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
public class SecurityConfig {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Constructs SecurityConfig with necessary dependencies.
     * 
     * @param userRepository Repository for user data, injected by Spring.
     */
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Configures the password encoder for the application.
     * 
     * @return BCryptPasswordEncoder for secure password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Sets up the UserDetailsService for authentication.
     * 
     * This service retrieves user details from the database and constructs
     * a Spring Security User object with appropriate roles.
     * 
     * @return A custom UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                return userRepository.findByUsername(username)
                    .map(user -> User.withUsername(user.getUsername())
                                    .password(user.getPassword())
                                    .roles(user.getRole().name())
                                    .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            } catch (Exception e) {
                logger.error("Error during user authentication for username: {}", username, e);
                throw new UsernameNotFoundException("Authentication error", e);
            }
        };
    }

    /**
     * Configures the SecurityFilterChain for HTTP security.
     * 
     * This method sets up various security configurations including:
     * - HTTPS channel requirement
     * - URL-based authorization rules
     * - Custom login and logout behavior
     * - Exception handling for access denied scenarios
     * - Session management policies
     * 
     * @param http HttpSecurity object to be configured.
     * @return Configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Require HTTPS for all requests
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public access paths
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.png", "/*.ico", "/h2-console/**").permitAll()
                .requestMatchers("/", "/login").permitAll()
                
                // Role-based access control
                .requestMatchers("/api/overall-productivity", "/view-responses", "/user-productivity", "/api/user-productivity/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/api/checker/**").hasRole("CHECKER")
                .requestMatchers("/api/shipping/**").hasRole("SHIPPING")
                .requestMatchers("/api/inventory/**").hasRole("INVENTORY")
                .requestMatchers("/packmed", "/api/packmed/**").hasAnyRole("CHECKER", "MODERATOR", "ADMIN")
                .requestMatchers("/api/user-productivity-stream").hasAnyRole("ADMIN", "MODERATOR")
                
                // Authenticated access for specific endpoints
                .requestMatchers(HttpMethod.POST, "/submit-questions").authenticated()
                
                // Default rule: require authentication for any other request
                .anyRequest().authenticated()
            )
            
            // Configure form login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureHandler((request, response, exception) -> {
                    logger.warn("Failed login attempt: {}", exception.getMessage());
                    response.sendRedirect("/login?error");
                })
                .permitAll()
            )
            
            // Configure logout behavior
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            
            // Handle access denied scenarios
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    try (PrintWriter writer = response.getWriter()) {
                        logger.warn("Access denied: {}", accessDeniedException.getMessage());
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        writer.write("{\"error\": \"ACCESS_DENIED\", \"message\": \"You do not have permission to access this resource\"}");
                        writer.flush();
                    } catch (IOException e) {
                        logger.error("Error writing access denied response", e);
                    }
                })
            )
            
            // Configure session management
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            );

        return http.build();
    }

    /**
     * Configures the AuthenticationManager.
     * 
     * @param authConfig AuthenticationConfiguration to be used.
     * @return Configured AuthenticationManager.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures global security settings.
     * 
     * This method sets up the UserDetailsService and PasswordEncoder
     * to be used by the AuthenticationManagerBuilder.
     * 
     * @param auth AuthenticationManagerBuilder to be configured.
     * @throws Exception if an error occurs during configuration.
     */
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }
}
