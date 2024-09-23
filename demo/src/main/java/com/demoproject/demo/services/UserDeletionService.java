package com.demoproject.demo.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demoproject.demo.repository.UserRepository;
import com.demoproject.demo.entity.User;
import java.util.Optional;

@Service
public class UserDeletionService {
    private final UserRepository userRepository;

    public UserDeletionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Check if the user is an admin
            if (User.Role.ADMIN == user.getRole()) {  // Change this line
                throw new IllegalStateException("Admin users cannot be deleted");
            }
            
            // Perform the deletion
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException("User not found: " + username);
        }
    }
}