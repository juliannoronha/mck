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
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import java.time.LocalDate;
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

    @OneToOne(mappedBy = "userAnswer", fetch = FetchType.LAZY)
    private Pac pac;
}