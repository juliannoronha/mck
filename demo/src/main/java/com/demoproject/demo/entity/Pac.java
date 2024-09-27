package com.demoproject.demo.entity;

import com.demoproject.demo.config.TruncatedDateTimeConverter;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    @Column(name = "submission_date")
    @Convert(converter = TruncatedDateTimeConverter.class)
    private LocalDateTime submissionDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "pouches_checked")
    private Integer pouchesChecked;

    @Column(name = "store", nullable = false)
    private String store;

    @PrePersist
    protected void onCreate() {
        submissionDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}