package com.demoproject.demo.dto;

public class UserProductivityDTO {
    private String username;
    private Long totalSubmissions;
    private String avgTimeDuration;
    private Double avgPouchesPerHour;
    private Long totalPouchesChecked;
    private Double avgPouchesChecked;

    public UserProductivityDTO(String username, Long totalSubmissions, String avgTimeDuration, 
                               Double avgPouchesPerHour, Long totalPouchesChecked, Double avgPouchesChecked) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.avgTimeDuration = avgTimeDuration;
        this.avgPouchesPerHour = avgPouchesPerHour;
        this.totalPouchesChecked = totalPouchesChecked;
        this.avgPouchesChecked = avgPouchesChecked;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public String getAvgTimeDuration() {
        return avgTimeDuration;
    }

    public void setAvgTimeDuration(String avgTimeDuration) {
        this.avgTimeDuration = avgTimeDuration;
    }

    public Double getAvgPouchesPerHour() {
        return avgPouchesPerHour;
    }

    public void setAvgPouchesPerHour(Double avgPouchesPerHour) {
        this.avgPouchesPerHour = avgPouchesPerHour;
    }

    public Long getTotalPouchesChecked() {
        return totalPouchesChecked;
    }

    public void setTotalPouchesChecked(Long totalPouchesChecked) {
        this.totalPouchesChecked = totalPouchesChecked;
    }

    public Double getAvgPouchesChecked() {
        return avgPouchesChecked;
    }

    public void setAvgPouchesChecked(Double avgPouchesChecked) {
        this.avgPouchesChecked = avgPouchesChecked;
    }
}