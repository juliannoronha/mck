package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.services.UserProductivityService;
import com.demoproject.demo.services.UserService;
import com.demoproject.demo.repository.PacRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller responsible for handling productivity-related requests.
 * This class provides endpoints for retrieving and streaming productivity data.
 */
@Controller
public class ProductivityController {
    private static final Logger logger = LoggerFactory.getLogger(ProductivityController.class);

    private final UserProductivityService userProductivityService;
    private final UserService userService;
    private final PacRepository pacRepository;

    /**
     * Constructor for ProductivityController.
     * @param userProductivityService Service for handling user productivity operations
     * @param userService Service for handling user-related operations
     * @param pacRepository Repository for handling Pac-related operations
     */
    public ProductivityController(UserProductivityService userProductivityService, UserService userService, PacRepository pacRepository) {
        this.userProductivityService = userProductivityService;
        this.userService = userService;
        this.pacRepository = pacRepository;
    }

    /**
     * Retrieves overall productivity data.
     * @return ResponseEntity containing overall productivity data
     */
    @GetMapping("/api/overall-productivity")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<UserProductivityDTO> getOverallProductivity() {
        logger.info("Fetching overall productivity");
        try {
            UserProductivityDTO overallProductivity = userProductivityService.getOverallProductivity();
            Map<String, Object> chartData = generateChartData();
            
            logger.debug("Overall productivity: {}", overallProductivity);
            logger.debug("Chart data: {}", chartData);
            
            UserProductivityDTO productivityWithChartData = new UserProductivityDTO(
                overallProductivity.getUsername(),
                overallProductivity.getTotalSubmissions(),
                overallProductivity.getTotalPouchesChecked(),
                overallProductivity.getAvgTimePerPouch(),
                overallProductivity.getAvgPouchesPerHour(),
                chartData
            );
            
            logger.debug("Final productivity DTO: {}", productivityWithChartData);
            return ResponseEntity.ok(productivityWithChartData);
        } catch (Exception e) {
            logger.error("Error fetching overall productivity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UserProductivityDTO("Error", 0, 0, 0, 0, null));
        }
    }

    /**
     * Displays user productivity page with paginated results.
     * @param model Model object for adding attributes
     * @param page Page number (default: 0)
     * @param size Number of items per page (default: 10)
     * @return Name of the view to render
     */
    @GetMapping("/user-productivity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String userProductivity(Model model, 
                                   @RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching user productivity for all users. Page: {}, Size: {}", page, size);
        Page<UserProductivityDTO> usersPage = userProductivityService.getAllUserProductivity(page, size);
        logger.debug("Retrieved {} user productivity records", usersPage.getContent().size());
        model.addAttribute("usersPage", usersPage);
        return "user-productivity";
    }

    /**
     * Retrieves productivity data for a specific user.
     * @param username Username of the user
     * @return ResponseEntity containing user's productivity data
     */
    @GetMapping("/api/user-productivity/{username}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Map<String, Object>> getUserProductivity(@PathVariable String username) {
        logger.info("Fetching user productivity for username: {}", username);
        Map<String, Object> productivity = userService.getUserProductivity(username);
        logger.debug("User productivity for {}: {}", username, productivity);
        return ResponseEntity.ok(productivity);
    }

    /**
     * Establishes a Server-Sent Events connection for streaming user productivity updates.
     * @return SseEmitter for streaming updates
     */
    @GetMapping(value = "/api/user-productivity-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserProductivity() {
        logger.info("New SSE connection established for user productivity");
        return userProductivityService.subscribeToProductivityUpdates();
    }

    /**
     * Retrieves paginated productivity data for all users.
     * @param page Page number (default: 0)
     * @param size Number of items per page (default: 10)
     * @return ResponseEntity containing paginated user productivity data
     */
    @GetMapping("/api/all-user-productivity")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<UserProductivityDTO>> getAllUserProductivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching all user productivity data. Page: {}, Size: {}", page, size);
        Page<UserProductivityDTO> productivityData = userService.getAllUserProductivity(page, size);
        logger.debug("Retrieved {} user productivity records", productivityData.getContent().size());
        return ResponseEntity.ok(productivityData);
    }

    /**
     * Establishes a Server-Sent Events connection for streaming overall productivity updates.
     * @return SseEmitter for streaming updates
     */
    @GetMapping(value = "/api/overall-productivity-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOverallProductivity() {
        logger.info("New SSE connection established for overall productivity");
        return userProductivityService.subscribeToOverallProductivityUpdates();
    }

    // TODO: Consider adding endpoints for productivity analytics and reporting
    // TODO: Implement caching strategy for frequently accessed productivity data

    private Map<String, Object> generateChartData() {
        try {
            Map<String, Object> chartData = new HashMap<>();
            List<String> labels = new ArrayList<>();
            List<Integer> pouchesChecked = new ArrayList<>();

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            logger.debug("Fetching data from {} to {}", startDate, endDate);

            List<Object[]> results = pacRepository.getPouchesCheckedLast7Days(startDate, endDate);
            logger.debug("Results from getPouchesCheckedLast7Days: {}", results);

            if (results.isEmpty()) {
                logger.info("No data available for the last 7 days. Generating sample data.");
                Random random = new Random();
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    labels.add(date.format(DateTimeFormatter.ofPattern("MMM dd")));
                    pouchesChecked.add(random.nextInt(100) + 1);
                }
            } else {
                Map<LocalDate, Long> resultMap = new HashMap<>();
                for (Object[] row : results) {
                    if (row[0] instanceof java.sql.Date) {
                        LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                        Long count = (Long) row[1];
                        resultMap.put(date, count);
                    }
                }

                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    labels.add(date.format(DateTimeFormatter.ofPattern("MMM dd")));
                    pouchesChecked.add(resultMap.getOrDefault(date, 0L).intValue());
                }
            }

            chartData.put("labels", labels);
            chartData.put("pouchesChecked", pouchesChecked);
            logger.debug("Generated chart data: {}", chartData);
            return chartData;
        } catch (Exception e) {
            logger.error("Error generating chart data", e);
            return new HashMap<>();
        }
    }
}
