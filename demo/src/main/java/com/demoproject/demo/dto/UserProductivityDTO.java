package com.demoproject.demo.dto;

public class UserProductivityDTO {
    private String username;
    private int totalSubmissions;
    private String avgTimeDuration;
    private double avgPouchesPerHour;

    // Constructor
    public UserProductivityDTO(String username, int totalSubmissions, String avgTimeDuration, double avgPouchesPerHour) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.avgTimeDuration = avgTimeDuration;
        this.avgPouchesPerHour = avgPouchesPerHour;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public String getAvgTimeDuration() {
        return avgTimeDuration;
    }

    public void setAvgTimeDuration(String avgTimeDuration) {
        this.avgTimeDuration = avgTimeDuration;
    }

    public double getAvgPouchesPerHour() {
        return avgPouchesPerHour;
    }

    public void setAvgPouchesPerHour(double avgPouchesPerHour) {
        this.avgPouchesPerHour = avgPouchesPerHour;
    }
}