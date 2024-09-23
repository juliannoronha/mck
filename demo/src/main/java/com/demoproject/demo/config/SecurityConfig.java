package com.demoproject.demo.config;

import com.demoproject.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;

/**
 * Configuration class for Spring Security settings.
 * This class sets up the security configuration for the application,
 * including authentication, authorization, and other security-related beans.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
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
            .map(user -> {
                return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
            })
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
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.POST, "/submit-questions").authenticated()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/*.png", "/*.ico", "/h2-console/**").permitAll()
                .requestMatchers("/", "/login").permitAll()
                .requestMatchers("/api/overall-productivity").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/view-responses").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/user-productivity").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/api/user-productivity/**").hasAnyRole("ADMIN", "MODERATOR")
                .requestMatchers("/api/checker/**").hasRole("CHECKER")
                .requestMatchers("/api/shipping/**").hasRole("SHIPPING")
                .requestMatchers("/api/inventory/**").hasRole("INVENTORY")
                .requestMatchers("/packmed").hasAnyRole("CHECKER", "MODERATOR", "ADMIN")
                .requestMatchers("/api/packmed/**").hasAnyRole("CHECKER", "MODERATOR", "ADMIN")
                .requestMatchers("/api/user-productivity-stream").hasAnyRole("ADMIN", "MODERATOR")
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
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("ACCESS_DENIED");
                    response.getWriter().flush();
                })
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
