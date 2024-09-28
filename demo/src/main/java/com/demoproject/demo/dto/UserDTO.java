package com.demoproject.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entities.
 * Used for transferring user data between layers without exposing entity details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * Must not be empty.
     */
    @NotEmpty(message = "Role cannot be empty")
    private String role;
}