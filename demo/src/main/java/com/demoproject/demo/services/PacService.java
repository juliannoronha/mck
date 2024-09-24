package com.demoproject.demo.services;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.repository.PacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacService {

    private final PacRepository pacRepository;

    public PacService(PacRepository pacRepository) {
        this.pacRepository = pacRepository;
    }

    @Transactional
    public Pac savePac(Pac pac) {
        return pacRepository.save(pac);
    }

    public Pac getPacById(Long id) {
        return pacRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deletePac(Long id) {
        pacRepository.deleteById(id);
    }
}