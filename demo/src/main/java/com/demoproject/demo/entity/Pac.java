package com.demoproject.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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

    @OneToOne
    @JoinColumn(name = "user_answer_id")
    private UserAnswer userAnswer;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "pouches_checked")
    private Integer pouchesChecked;
}