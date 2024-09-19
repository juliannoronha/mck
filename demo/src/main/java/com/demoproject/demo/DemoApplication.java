package com.demoproject.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import com.demoproject.demo.repository.RoleRepository;
import com.demoproject.demo.entity.Role;
import com.demoproject.demo.services.UserService;

@SpringBootApplication
public class DemoApplication {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                userService.createRole("USER");
                userService.createRole("ADMIN");
            }
        };
    }
}
