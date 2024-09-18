package com.demoproject.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalTime;

/**
 * Entity class representing a user's answer in the system.
 * This class is mapped to a database table using JPA annotations.
 */
@Entity
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
    @Column(nullable = false)
    private String name;

    /**
     * The time when the user started their task.
     */
    @Column(nullable = false)
    private LocalTime startTime;

    /**
     * The time when the user finished their task.
     */
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * The store associated with this user answer.
     */
    @Column(nullable = false)
    private String store;

    /**
     * The number of pouches checked by the user.
     */
    @Column(nullable = false)
    private Integer pouchesChecked;

    // Getters and setters
    /**
     * Gets the unique identifier of the user answer.
     * @return The ID of the user answer.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user answer.
     * @param id The ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name associated with this user answer.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name associated with this user answer.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the start time of the task.
     * @return The start time.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the task.
     * @param startTime The start time to set.
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time of the task.
     * @return The end time.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the task.
     * @param endTime The end time to set.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the store associated with this user answer.
     * @return The store.
     */
    public String getStore() {
        return store;
    }

    /**
     * Sets the store associated with this user answer.
     * @param store The store to set.
     */
    public void setStore(String store) {
        this.store = store;
    }

    /**
     * Gets the number of pouches checked.
     * @return The number of pouches checked.
     */
    public Integer getPouchesChecked() {
        return pouchesChecked;
    }

    /**
     * Sets the number of pouches checked.
     * @param pouchesChecked The number of pouches checked to set.
     */
    public void setPouchesChecked(Integer pouchesChecked) {
        this.pouchesChecked = pouchesChecked;
    }
}