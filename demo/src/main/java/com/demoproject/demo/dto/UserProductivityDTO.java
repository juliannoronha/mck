package com.demoproject.demo.dto;

import lombok.Value;

@Value
public class UserProductivityDTO {
    String username;
    long totalSubmissions;
    long totalPouchesChecked;
    String avgTimeDuration;
    double avgPouchesPerHour;
}