package com.demoproject.demo.dto;

public class UserProductivityQueryDTO {
    private String username;
    private Long totalSubmissions;
    private Double avgPouchesChecked;

    public UserProductivityQueryDTO(String username, Long totalSubmissions, Double avgPouchesChecked) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.avgPouchesChecked = avgPouchesChecked;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public Double getAvgPouchesChecked() {
        return avgPouchesChecked;
    }
}