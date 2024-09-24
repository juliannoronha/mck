package com.demoproject.demo.entity; // Adjust this package name as needed

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "users") // Specify the table name, "user" is often a reserved keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public enum Role {
        ADMIN, MODERATOR, USER, CHECKER, SHIPPING, INVENTORY
    }

    // Constructor with fields
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = Role.valueOf(role.toUpperCase());
    }

    // Getters and setters are handled by Lombok's @Data annotation
    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}