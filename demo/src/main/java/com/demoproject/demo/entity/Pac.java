package com.demoproject.demo.entity;

import com.demoproject.demo.config.TruncatedDateTimeConverter;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a Pouch Accuracy Check (PAC) entity in the system.
 * This class encapsulates data related to a single PAC session, including
 * user information, timing details, and productivity metrics.
 */
@Entity
@Table(name = "pac")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who performed this PAC session.
     * Lazy fetching is used to optimize performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The date and time when this PAC was submitted.
     * Uses a custom converter to truncate the timestamp for consistency.
     */
    @Column(name = "submission_date")
    @Convert(converter = TruncatedDateTimeConverter.class)
    private LocalDateTime submissionDate;

    /**
     * The time when the PAC session started.
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * The time when the PAC session ended.
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    /**
     * The number of pouches checked during this PAC session.
     */
    @Column(name = "pouches_checked")
    private Integer pouchesChecked;

    /**
     * The store identifier where this PAC was performed.
     */
    @Column(name = "store", nullable = false)
    private String store;

    /**
     * Automatically sets the submission date to the current time
     * when a new PAC entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        submissionDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // TODO: Consider adding a method to calculate the duration of the PAC session
    // TODO: Implement validation to ensure end time is after start time
}