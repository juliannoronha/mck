package com.demoproject.demo.dto;

public class UserProductivityDTO {
    private String username;
    private int totalSubmissions;
    private String avgTimeDuration;
    private Double avgPouchesPerHour;
    private int totalPouchesChecked;
    private Double avgPouchesChecked;  // Add this line

    // Update the constructor
    public UserProductivityDTO(String username, int totalSubmissions, String avgTimeDuration, 
                               Double avgPouchesPerHour, int totalPouchesChecked, Double avgPouchesChecked) {
        this.username = username;
        this.totalSubmissions = totalSubmissions;
        this.avgTimeDuration = avgTimeDuration;
        this.avgPouchesPerHour = avgPouchesPerHour;
        this.totalPouchesChecked = totalPouchesChecked;
        this.avgPouchesChecked = avgPouchesChecked;  // Add this line
    }

    // Add getter and setter for the new field
    public int getTotalPouchesChecked() {
        return totalPouchesChecked;
    }

    public void setTotalPouchesChecked(int totalPouchesChecked) {
        this.totalPouchesChecked = totalPouchesChecked;
    }

    // Add getter for avgPouchesChecked
    public Double getAvgPouchesChecked() {
        return avgPouchesChecked;
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