package com.demoproject.demo.controller;

import com.demoproject.demo.entity.Pac;
import com.demoproject.demo.services.ResponseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.demoproject.demo.annotation.RequiresAuthentication;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controller responsible for handling responses related to the packmed system.
 * This includes submitting questions, viewing responses, and managing user interactions.
 */
@Controller
public class ResponseController {

    private final ResponseService responseService;
    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    /**
     * Displays the questions page for authenticated users.
     * @return The name of the view to render
     */
    @GetMapping("/questions")
    @RequiresAuthentication
    public String showQuestions() {
        return "packmed";
    }

    /**
     * Handles the submission of questions from authenticated users.
     * @param pacData Map containing the PAC (Pouch Accuracy Check) data
     * @param authentication The authentication object of the current user
     * @return ResponseEntity with the result of the submission
     */
    @PostMapping("/submit-questions")
    @RequiresAuthentication
    @ResponseBody
    public ResponseEntity<String> submitQuestions(@RequestBody Map<String, String> pacData, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized access attempt to submit questions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            // Parse and create Pac object from submitted data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Pac pac = new Pac();
            pac.setStore(pacData.get("store"));
            pac.setStartTime(LocalTime.parse(pacData.get("startTime").split("T")[1], formatter));
            pac.setEndTime(LocalTime.parse(pacData.get("endTime").split("T")[1], formatter));
            pac.setPouchesChecked(Integer.parseInt(pacData.get("pouchesChecked")));

            responseService.submitUserAnswer(pac, authentication.getName());

            return ResponseEntity.ok("Questions submitted successfully");
        } catch (Exception e) {
            logger.error("Error submitting questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting questions: " + e.getMessage());
        }
    }

    /**
     * Deletes a response. Only accessible by ADMIN or MODERATOR roles.
     * @param id The ID of the response to delete
     * @return ResponseEntity with the result of the deletion
     */
    @PostMapping("/delete-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<String> deleteResponse(@RequestParam Long id) {
        try {
            boolean deleted = responseService.deleteResponse(id);
            if (deleted) {
                return ResponseEntity.ok("Response deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("Response not found with id: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting response: " + e.getMessage());
        }
    }

    /**
     * Displays a paginated and filtered list of responses. Only accessible by ADMIN or MODERATOR roles.
     * @param model The Model object to add attributes
     * @param page The page number (default 0)
     * @param size The page size (default 10)
     * @param nameFilter Optional filter for user name
     * @param store Optional filter for store
     * @param month Optional filter for month
     * @return The name of the view to render
     */
    @GetMapping("/view-responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String viewResponses(Model model, 
                            @RequestParam(defaultValue = "0") int page, 
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String nameFilter,
                            @RequestParam(required = false) String store,
                            @RequestParam(required = false) String month) {
        try {
            // Parse month filter if provided
            Integer monthValue = (month != null && !month.isEmpty() && !month.equals("null")) 
                ? Integer.parseInt(month) : null;

            // Create pageable object with sorting
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionDate"));
            
            // Fetch filtered and paginated responses
            Page<Pac> responsesPage = responseService.getAllResponsesWithFilters(
                pageable, nameFilter, store, monthValue);
            
            // Add attributes to the model for rendering in the view
            model.addAttribute("responses", responsesPage);
            model.addAttribute("nameFilter", nameFilter);
            model.addAttribute("selectedStore", store);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", responsesPage.getTotalPages());
            model.addAttribute("totalItems", responsesPage.getTotalElements());
            
            return "responses";
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format: {}", e.getMessage());
            return "error";
        }
    }

    // TODO: Consider adding an endpoint for exporting responses to CSV or Excel
    // TODO: Implement caching for frequently accessed response data to improve performance
}