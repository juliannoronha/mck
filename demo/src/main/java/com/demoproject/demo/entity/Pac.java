package com.demoproject.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "pac")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pouches_checked")
    private Integer pouchesChecked;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "store", nullable = false)
    private String store;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;  // Add this if it doesn't exist

    // Remove the UserAnswer relationship
}