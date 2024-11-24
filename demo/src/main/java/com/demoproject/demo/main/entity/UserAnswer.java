package com.demoproject.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a user's answer in the system.
 * 
 * This entity is mapped to the 'user_answer' table in the database.
 * It contains information about the answer, including its unique identifier,
 * associated name, submission date, and the user who provided the answer.
 *
 * @see User
 */
@Entity
@Table(name = "user_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {

    /**
     * Unique identifier for the user answer.
     * Automatically generated using identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name associated with this user answer.
     * This could represent the title or a brief description of the answer.
     */
    private String name;

    /**
     * The date when the answer was submitted.
     * Stored as a LocalDate for easy date manipulations.
     */
    private LocalDate submissionDate;

    /**
     * The user who provided this answer.
     * This field establishes a Many-to-One relationship with the User entity.
     * 
     * The fetch type is set to LAZY to optimize performance by loading
     * the associated User only when explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // TODO: Consider adding a field for the actual answer content
    // private String answerContent;

    // TODO: Implement validation for the name and submissionDate fields
}