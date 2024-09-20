package com.demoproject.demo.dto;

import com.demoproject.demo.entity.User;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for User entities.
 * Used for transferring user data between layers without exposing entity details.
 */
public class UserDTO {
    /**
     * The username of the user.
     * Must not be empty and should be between 3 and 50 characters long.
     */
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * The password of the user.
     * Must not be empty and should be between 6 and 100 characters long.
     */
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * The role of the user.
     * Must not be null.
     */
    @NotNull(message = "Role cannot be null")
    private User.Role role;

    /**
     * No-args constructor for UserDTO.
     */
    public UserDTO() {}

    /**
     * Constructor with all fields for UserDTO.
     *
     * @param username The username of the user
     * @param password The password of the user
     * @param role The role of the user as a string (will be converted to User.Role enum)
     */
    public UserDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = User.Role.valueOf(role.toUpperCase());
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the role.
     *
     * @return The role
     */
    public User.Role getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role The role to set
     */
    public void setRole(User.Role role) {
        this.role = role;
    }
}