package com.demoproject.demo.dto;

import lombok.Value;

/**
 * Data Transfer Object (DTO) representing user productivity metrics.
 * This class encapsulates various productivity indicators for a user.
 */
@Value
public class UserProductivityDTO {
    /** The username of the user whose productivity is being measured. */
    String username;

    /** Total number of submissions made by the user. */
    long totalSubmissions;

    /** Total number of pouches checked by the user. */
    long totalPouchesChecked;

    /** Average time per pouch in seconds. */
    double avgTimePerPouch;

    /** Average number of pouches processed per hour. */
    double avgPouchesPerHour;
}