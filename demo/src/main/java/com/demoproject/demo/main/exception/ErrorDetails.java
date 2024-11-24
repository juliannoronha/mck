package com.demoproject.demo.exception;

import java.time.LocalDateTime;

/**
 * Represents the details of an error occurrence in the application.
 * This class encapsulates information about when an error occurred,
 * what the error message was, and any additional details.
 */
public class ErrorDetails {
    private LocalDateTime timestamp; // The time when the error occurred
    private String message;          // A brief description of the error
    private String details;          // Additional information about the error

    /**
     * Constructs a new ErrorDetails object.
     *
     * @param timestamp The time when the error occurred
     * @param message   A brief description of the error
     * @param details   Additional information about the error
     */
    public ErrorDetails(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    // Getter methods
    /**
     * @return The timestamp of when the error occurred
     */
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * @return The error message
     */
    public String getMessage() { return message; }

    /**
     * @return Additional details about the error
     */
    public String getDetails() { return details; }

    // TODO: Consider adding setter methods if error details need to be modified after creation
    // TODO: Implement toString() method for easy logging and debugging
}