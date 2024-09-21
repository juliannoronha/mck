package com.demoproject.demo.dto;

public class UserProductivityQueryDTO {
    private String username;
    private Long totalSubmissions;
    private Double avgPouchesChecked;
    private Long totalPouchesChecked; // New field

    public UserProductivityQueryDTO(String username, Long totalSubmissions, Double avgPouchesChecked, Long totalPouchesChecked) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.avgPouchesChecked = avgPouchesChecked;
        this.totalPouchesChecked = totalPouchesChecked;
    }

    // Existing getters
    public String getUsername() {
        return username;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public Double getAvgPouchesChecked() {
        return avgPouchesChecked;
    }

    // New getter for totalPouchesChecked
    public Long getTotalPouchesChecked() {
        return totalPouchesChecked;
    }
}