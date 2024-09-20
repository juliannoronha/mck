package com.demoproject.demo.config;

import com.demoproject.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

/**
 * Configuration class for Spring Security settings.
 * This class sets up the security configuration for the application,
 * including authentication, authorization, and other security-related beans.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final UserRepository userRepository;

    /**
     * Constructor for SecurityConfig.
     * @param userRepository Repository for user data, injected by Spring.
     */
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Defines the password encoder bean.
     * @return BCryptPasswordEncoder for secure password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the UserDetailsService for authentication.
     * @return A UserDetailsService that retrieves user details from the database.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name()) // Convert the Role enum to a String
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Configures the SecurityFilterChain for HTTP security.
     * @param http HttpSecurity object to be configured.
     * @return Configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.png", "/*.ico", "/h2-console/**").permitAll()
                .requestMatchers("/", "/login").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
    }

    /**
     * Configures the AuthenticationManager.
     * @param authConfig AuthenticationConfiguration to be used.
     * @return Configured AuthenticationManager.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }
}
