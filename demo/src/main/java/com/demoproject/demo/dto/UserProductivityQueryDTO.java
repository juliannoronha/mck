package com.demoproject.demo.dto;

public class UserProductivityQueryDTO {
    private String username;
    private Long totalSubmissions;
    private Long totalPouchesChecked;
    private Long totalMinutes;

    public UserProductivityQueryDTO(String username, Long totalSubmissions, Long totalPouchesChecked, Long totalMinutes) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.totalPouchesChecked = totalPouchesChecked;
        this.totalMinutes = totalMinutes;
    }

    // Getters
    public String getUsername() { return username; }
    public Long getTotalSubmissions() { return totalSubmissions; }
    public Long getTotalPouchesChecked() { return totalPouchesChecked; }
    public Long getTotalMinutes() { return totalMinutes; }
}