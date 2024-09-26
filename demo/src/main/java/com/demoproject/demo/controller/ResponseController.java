package com.demoproject.demo.controller;

import com.demoproject.demo.entity.UserAnswer;
import com.demoproject.demo.entity.Pac;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.demoproject.demo.annotation.RequiresAuthentication;
import com.demoproject.demo.services.ResponseService;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;  
import java.time.format.DateTimeParseException;

@Controller
public class ResponseController {

    private final ResponseService responseService;
    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping("/questions")
    @RequiresAuthentication
    public String showQuestions() {
        return "packmed";
    }

    @PostMapping("/submit-questions")
    @RequiresAuthentication
    @ResponseBody
    public ResponseEntity<String> submitQuestions(@RequestBody Map<String, String> pacData, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized access attempt to submit questions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            Pac pac = new Pac();
            pac.setStore(pacData.get("store"));
            pac.setStartTime(LocalTime.parse(pacData.get("startTime").split("T")[1], formatter));
            pac.setEndTime(LocalTime.parse(pacData.get("endTime").split("T")[1], formatter));
            pac.setPouchesChecked(Integer.parseInt(pacData.get("pouchesChecked")));

            UserAnswer userAnswer = new UserAnswer();
            responseService.submitUserAnswer(userAnswer, pac, authentication.getName());
            logger.info("User {} successfully submitted a response", authentication.getName());
            return ResponseEntity.ok("Response submitted successfully!");
        } catch (DateTimeParseException e) {
            logger.error("Error parsing time input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid time format provided");
        } catch (NumberFormatException e) {
            logger.error("Error parsing pouches checked: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid number format for pouches checked");
        } catch (Exception e) {
            logger.error("Error submitting questions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while submitting questions");
        }
    }

    @PostMapping("/delete-response")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<String> deleteResponse(@RequestParam Long id) {
        try {
            responseService.deleteResponse(id);
            return ResponseEntity.ok("Response deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace to the console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting response: " + e.getMessage() + ". Check server logs for details.");
        }
    }

    @GetMapping("/view-responses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String viewResponses(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<UserAnswer> responses = responseService.getAllResponsesWithPacSortedByDateDesc(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submissionDate")));
        model.addAttribute("responses", responses);
        return "responses";
    }
}