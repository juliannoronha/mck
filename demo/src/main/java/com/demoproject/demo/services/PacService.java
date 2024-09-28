package com.demoproject.demo.services;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Pac (Pouch Accuracy Check) entities.
 * This class provides methods for CRUD operations on Pac objects.
 */
@Service
public class PacService {

    // Repository for Pac entity operations
    private final PacRepository pacRepository;

    /**
     * Constructs a new PacService with the given PacRepository.
     * 
     * @param pacRepository The repository to be used for Pac operations
     */
    public PacService(PacRepository pacRepository) {
        this.pacRepository = pacRepository;
    }

    /**
     * Saves a new Pac entity or updates an existing one.
     * 
     * @param pac The Pac object to be saved
     * @return The saved Pac object
     */
    @Transactional
    public Pac savePac(Pac pac) {
        return pacRepository.save(pac);
    }

    /**
     * Retrieves a Pac entity by its ID.
     * 
     * @param id The ID of the Pac to retrieve
     * @return The Pac object if found, null otherwise
     */
    public Pac getPacById(Long id) {
        return pacRepository.findById(id).orElse(null);
    }

    /**
     * Deletes a Pac entity by its ID.
     * 
     * @param id The ID of the Pac to delete
     */
    @Transactional
    public void deletePac(Long id) {
        pacRepository.deleteById(id);
    }

    // TODO: Implement method to retrieve Pacs by user or date range
    // TODO: Add validation logic for Pac entities before saving
    // TODO: Consider implementing a method to update specific fields of a Pac
}