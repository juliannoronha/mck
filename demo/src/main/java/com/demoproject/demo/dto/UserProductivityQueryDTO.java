package com.demoproject.demo.dto;

/**
 * Data Transfer Object (DTO) for querying user productivity metrics.
 * This class encapsulates key productivity indicators for a user.
 */
public class UserProductivityQueryDTO {
    private String username;
    private Long totalSubmissions;
    private Long totalPouchesChecked;
    private Long totalMinutes;

    /**
     * Constructs a new UserProductivityQueryDTO with the given parameters.
     *
     * @param username            The username of the user
     * @param totalSubmissions    Total number of submissions made by the user
     * @param totalPouchesChecked Total number of pouches checked by the user
     * @param totalMinutes        Total time spent on tasks in minutes
     */
    public UserProductivityQueryDTO(String username, Long totalSubmissions, Long totalPouchesChecked, Long totalMinutes) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.totalPouchesChecked = totalPouchesChecked;
        this.totalMinutes = totalMinutes;
    }

    // Getters
    /** @return The username of the user */
    public String getUsername() { return username; }

    /** @return The total number of submissions made by the user */
    public Long getTotalSubmissions() { return totalSubmissions; }

    /** @return The total number of pouches checked by the user */
    public Long getTotalPouchesChecked() { return totalPouchesChecked; }

    /** @return The total time spent on tasks in minutes */
    public Long getTotalMinutes() { return totalMinutes; }

    // TODO: Consider adding a method to calculate productivity rate (e.g., pouches per hour)
    // TODO: Implement data validation to ensure all fields contain valid values
}