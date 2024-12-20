package com.demoproject.demo.services;

import com.demoproject.demo.entity.Wellca;
import com.demoproject.demo.repository.WellcaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WellcaService {
    private static final Logger logger = LoggerFactory.getLogger(WellcaService.class);
    private final WellcaRepository wellcaRepository;

    public WellcaService(WellcaRepository wellcaRepository) {
        this.wellcaRepository = wellcaRepository;
    }

    /**
     * Save or update a Wellca entry
     * @param wellca The entry to save
     * @return The saved entry
     */
    @Transactional
    @CacheEvict(value = "wellcaData", allEntries = true)
    public Wellca saveEntry(Wellca wellca) {
        logger.debug("Saving Wellca entry for date: {}", wellca.getDate());
        
        if (wellcaRepository.existsByDate(wellca.getDate())) {
            logger.info("Updating existing entry for date: {}", wellca.getDate());
        }
        
        return wellcaRepository.save(wellca);
    }

    /**
     * Retrieve entry by date
     * @param date The date to search for
     * @return Optional containing the entry if found
     */
    @Cacheable(value = "wellcaData", key = "#date")
    public Optional<Wellca> getEntryByDate(LocalDate date) {
        logger.debug("Fetching Wellca entry for date: {}", date);
        return wellcaRepository.findByDate(date);
    }

    /**
     * Get entries within a date range
     * @param startDate Start of the range
     * @param endDate End of the range
     * @return List of entries
     */
    @Cacheable(value = "wellcaRangeData", key = "#startDate + '-' + #endDate")
    public List<Wellca> getEntriesInRange(LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching Wellca entries between {} and {}", startDate, endDate);
        return wellcaRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);
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

        return Map.of(
            "averageProfilesEntered", avgProfilesEntered,
            "totalRxFilled", totalRx,
            "entriesCount", weeklyData.size()
        );
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
