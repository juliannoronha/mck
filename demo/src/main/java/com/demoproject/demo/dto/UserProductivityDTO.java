package com.demoproject.demo.dto;

public class UserProductivityDTO {
    private String username;
    private long totalSubmissions;
    private long totalPouchesChecked;
    private String avgTimeDuration;
    private double avgPouchesPerHour;
    private double avgPouchesChecked;

    public UserProductivityDTO(String username, long totalSubmissions, long totalPouchesChecked, 
                               String avgTimeDuration, double avgPouchesPerHour, double avgPouchesChecked) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.totalPouchesChecked = totalPouchesChecked;
        this.avgTimeDuration = avgTimeDuration;
        this.avgPouchesPerHour = avgPouchesPerHour;
        this.avgPouchesChecked = avgPouchesChecked;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getTotalPouchesChecked() {
        return totalPouchesChecked;
    }

    public void setTotalPouchesChecked(long totalPouchesChecked) {
        this.totalPouchesChecked = totalPouchesChecked;
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

    public double getAvgPouchesChecked() {
        return avgPouchesChecked;
    }

    public void setAvgPouchesChecked(double avgPouchesChecked) {
        this.avgPouchesChecked = avgPouchesChecked;
    }
}