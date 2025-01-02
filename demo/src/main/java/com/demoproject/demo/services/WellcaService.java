package com.demoproject.demo.services;

import com.demoproject.demo.entity.Wellca;
import com.demoproject.demo.repository.WellcaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"wellcaData"})
public class WellcaService {
    private static final Logger logger = LoggerFactory.getLogger(WellcaService.class);
    private final WellcaRepository wellcaRepository;
    private final CacheManager cacheManager;

    public WellcaService(WellcaRepository wellcaRepository, CacheManager cacheManager) {
        this.wellcaRepository = wellcaRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Save or update a Wellca entry
     * @param wellca The entry to save
     * @return The saved entry
     */
    @Transactional
    @CacheEvict(value = {"wellcaRangeData", "wellcaData"}, allEntries = true)
    public Wellca saveEntry(Wellca wellca) {
        logger.debug("Saving Wellca entry for date: {} with service type: {} and cost: {}", 
            wellca.getDate(), wellca.getServiceType(), wellca.getServiceCost());
        
        if (wellcaRepository.existsByDate(wellca.getDate())) {
            logger.info("Updating existing entry for date: {}", wellca.getDate());
        }
        
        Wellca savedEntry = wellcaRepository.save(wellca);
        logger.debug("Successfully saved entry with ID: {}. Service type: {}, cost: {}", 
            savedEntry.getId(), savedEntry.getServiceType(), savedEntry.getServiceCost());
        
        // Clear all related caches
        clearCaches();
        
        return savedEntry;
    }

    private void clearCaches() {
        logger.debug("Clearing all Wellca related caches");
        cacheManager.getCacheNames().stream()
            .filter(name -> name.startsWith("wellca"))
            .forEach(cacheName -> {
                logger.debug("Clearing cache: {}", cacheName);
                cacheManager.getCache(cacheName).clear();
            });
    }

    /**
     * Retrieve entry by date
     * @param date The date to search for
     * @return Optional containing the entry if found
     */
    @Cacheable(value = "wellcaData", key = "#date")
    public Optional<Wellca> getEntryByDate(LocalDate date) {
        logger.debug("Fetching Wellca entry for date: {}", date);
        Optional<Wellca> entry = wellcaRepository.findByDate(date);
        entry.ifPresent(e -> logger.debug("Found entry with service type: {} and cost: {}", 
            e.getServiceType(), e.getServiceCost()));
        return entry;
    }

    /**
     * Get entries within a date range
     * @param startDate Start of the range
     * @param endDate End of the range
     * @return List of entries
     */
    @Cacheable(value = "wellcaRangeData", key = "#startDate.toString() + '-' + #endDate.toString()")
    public List<Wellca> getEntriesInRange(LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching Wellca entries between {} and {}", startDate, endDate);
        List<Wellca> entries = wellcaRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);
        logger.debug("Found {} entries in date range", entries.size());
        entries.forEach(e -> logger.debug("Entry date: {}, service type: {}, cost: {}", 
            e.getDate(), e.getServiceType(), e.getServiceCost()));
        return entries;
    }

    /**
     * Get weekly statistics
     * @param weekStartDate First day of the week
     * @return Weekly aggregated data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklyStats(LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        List<Wellca> weeklyData = wellcaRepository.findWeeklyData(weekStartDate, weekEndDate);
        logger.debug("Fetching weekly stats for period {} to {}", weekStartDate, weekEndDate);
        return calculateWeeklyStats(weeklyData);
    }

    /**
     * Get service type statistics
     * @param startDate Start of period
     * @param endDate End of period
     * @return Service statistics
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getServiceTypeStats(LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching service type stats for period {} to {}", startDate, endDate);
        return wellcaRepository.getServiceTypeStats(startDate, endDate);
    }

    /**
     * Calculate weekly statistics from data
     * @param weeklyData List of entries for the week
     * @return Map of calculated statistics
     */
    private Map<String, Object> calculateWeeklyStats(List<Wellca> weeklyData) {
        double avgProfilesEntered = weeklyData.stream()
            .mapToInt(Wellca::getProfilesEntered)
            .average()
            .orElse(0.0);

        long totalRx = weeklyData.stream()
            .mapToInt(Wellca::getTotalFilled)
            .sum();

        Map<String, Object> stats = Map.of(
            "averageProfilesEntered", avgProfilesEntered,
            "totalRxFilled", totalRx,
            "entriesCount", weeklyData.size()
        );
        
        logger.debug("Calculated weekly stats: {}", stats);
        return stats;
    }

    /**
     * Delete an entry
     * @param id Entry ID to delete
     */
    @Transactional
    @CacheEvict(value = "wellcaData", allEntries = true)
    public void deleteEntry(Long id) {
        logger.debug("Deleting Wellca entry with ID: {}", id);
        wellcaRepository.deleteById(id);
    }

    /**
     * Get monthly delivery counts
     * @param yearMonth Year and month for the report
     * @return Monthly delivery statistics
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyDeliveryCounts(LocalDate yearMonth) {
        LocalDate startDate = yearMonth.withDayOfMonth(1);
        LocalDate endDate = yearMonth.withDayOfMonth(yearMonth.lengthOfMonth());
        
        logger.debug("Fetching monthly delivery counts for period: {} to {}", startDate, endDate);
        return wellcaRepository.getMonthlyDeliveryCounts(startDate, endDate);
    }
}
