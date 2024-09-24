package com.demoproject.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity class representing a user's answer in the system.
 * This class is mapped to a database table using JPA annotations.
 */
@Entity
@Table(name = "user_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    /**
     * Unique identifier for the user answer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name associated with this user answer.
     */
    private String name;

    /**
     * The time when the user started their task.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * The time when the user finished their task.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * The store associated with this user answer.
     */
    @Column(nullable = false, length = 20)
    private String store;

    /**
     * The number of pouches checked by the user.
     */
    @Column(name = "pouches_checked")
    private Integer pouchesChecked;

    /**
     * The date of submission.
     */
    private LocalDate submissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Add these setter methods
    public void setStartDateTime(String startDateTime) {
        this.startTime = LocalDateTime.parse(startDateTime);
    }

    public void setEndDateTime(String endDateTime) {
        this.endTime = LocalDateTime.parse(endDateTime);
    }

    public void setStartTime(String startTime) {
        this.startTime = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public void setEndTime(String endTime) {
        this.endTime = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}