package com.demoproject.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a User entity in the system.
 * This class encapsulates user data, including authentication details and associated entities.
 */
@Entity
@Table(name = "users") // "user" is often a reserved keyword, so we use "users"
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok: Generates a no-args constructor
@AllArgsConstructor // Lombok: Generates an all-args constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pac> pacs = new ArrayList<>();

    /**
     * Enum representing the possible roles a user can have in the system.
     */
    public enum Role {
        ADMIN, MODERATOR, USER, CHECKER, SHIPPING, INVENTORY
    }

    /**
     * Constructs a new User with the given username, password, and role.
     * 
     * @param username The user's unique username
     * @param password The user's password (should be encrypted before storage)
     * @param role The user's role in the system (case-insensitive)
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = Role.valueOf(role.toUpperCase());
    }

    // Note: Most getters and setters are handled by Lombok's @Data annotation

    /**
     * Gets the role of the user.
     * 
     * @return The user's role
     */
    public Role getRole() {
        return this.role;
    }

    /**
     * Sets the role of the user.
     * 
     * @param role The new role to assign to the user
     */
    public void setRole(Role role) {
        this.role = role;
    }

    // TODO: Consider adding methods for password encryption and validation
    // TODO: Implement user activity tracking (e.g., last login date)
}