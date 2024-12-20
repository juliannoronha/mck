package com.demoproject.demo.controller;

import com.demoproject.demo.dto.WellcaDTO;
import com.demoproject.demo.entity.Wellca;
import com.demoproject.demo.services.WellcaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wellca-management")
public class WellcaController {
    
    private static final Logger logger = LoggerFactory.getLogger(WellcaController.class);
    private final WellcaService wellcaService;

    public WellcaController(WellcaService wellcaService) {
        this.wellcaService = wellcaService;
    }

    /**
     * Display the main Wellca form
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String showWellcaForm(Model model) {
        model.addAttribute("wellcaDTO", new WellcaDTO());
        return "wellca";
    }

    /**
     * Submit new entry
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> submitEntry(@Valid @RequestBody WellcaDTO wellcaDTO, 
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                .body(bindingResult.getAllErrors());
        }

        try {
            Wellca savedEntry = wellcaService.saveEntry(convertToEntity(wellcaDTO));
            return ResponseEntity.ok(convertToDTO(savedEntry));
        } catch (Exception e) {
            logger.error("Error saving Wellca entry", e);
            return ResponseEntity.internalServerError()
                .body("Error saving entry: " + e.getMessage());
        }
    }

    /**
     * Get entry by date
     */
    @GetMapping("/entry/{date}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getEntryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return wellcaService.getEntryByDate(date)
            .map(entry -> ResponseEntity.ok(convertToDTO(entry)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get entries within date range
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<WellcaDTO>> getEntriesInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Wellca> entries = wellcaService.getEntriesInRange(startDate, endDate);
        return ResponseEntity.ok(entries.stream()
            .map(this::convertToDTO)
            .toList());
    }

    /**
     * Get weekly statistics
     */
    @GetMapping("/weekly-stats/{weekStartDate}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(wellcaService.getWeeklyStats(weekStartDate));
    }

    /**
     * Get monthly delivery statistics
     */
    @GetMapping("/monthly-delivery/{yearMonth}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyDeliveryCounts(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") LocalDate yearMonth) {
        return ResponseEntity.ok(wellcaService.getMonthlyDeliveryCounts(yearMonth));
    }

    /**
     * Delete entry
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        try {
            wellcaService.deleteEntry(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting Wellca entry", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert DTO to Entity
     */
    private Wellca convertToEntity(WellcaDTO dto) {
        Wellca entity = new Wellca();
        // Map DTO fields to entity
        entity.setId(dto.getId());
        entity.setDate(dto.getDate());
        entity.setPurolator(dto.getPurolator());
        entity.setFedex(dto.getFedex());
        entity.setOneCourier(dto.getOneCourier());
        entity.setGoBolt(dto.getGoBolt());
        entity.setNewRx(dto.getNewRx());
        entity.setRefill(dto.getRefill());
        entity.setReAuth(dto.getReAuth());
        entity.setHold(dto.getHold());
        entity.setProfilesEntered(dto.getProfilesEntered());
        entity.setWhoFilledRx(dto.getWhoFilledRx());
        entity.setActivePercentage(dto.getActivePercentage());
        entity.setServiceType(dto.getServiceType());
        entity.setServiceCost(dto.getServiceCost());
        return entity;
    }

    /**
     * Convert Entity to DTO
     */
    private WellcaDTO convertToDTO(Wellca entity) {
        WellcaDTO dto = new WellcaDTO();
        // Map entity fields to DTO
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setPurolator(entity.getPurolator());
        dto.setFedex(entity.getFedex());
        dto.setOneCourier(entity.getOneCourier());
        dto.setGoBolt(entity.getGoBolt());
        dto.setNewRx(entity.getNewRx());
        dto.setRefill(entity.getRefill());
        dto.setReAuth(entity.getReAuth());
        dto.setHold(entity.getHold());
        dto.setProfilesEntered(entity.getProfilesEntered());
        dto.setWhoFilledRx(entity.getWhoFilledRx());
        dto.setActivePercentage(entity.getActivePercentage());
        dto.setServiceType(entity.getServiceType());
        dto.setServiceCost(entity.getServiceCost());
        return dto;
    }
}
