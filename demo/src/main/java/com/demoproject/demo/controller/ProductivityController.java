package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.services.UserProductivityService;
import com.demoproject.demo.services.UserService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProductivityController {
    private static final Logger logger = LoggerFactory.getLogger(ProductivityController.class);

    private final UserProductivityService userProductivityService;
    private final UserService userService;

    public ProductivityController(UserProductivityService userProductivityService, UserService userService) {
        this.userProductivityService = userProductivityService;
        this.userService = userService;
    }

    @GetMapping("/api/overall-productivity")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Cacheable("overallProductivity")
    public ResponseEntity<UserProductivityDTO> getOverallProductivity() {
        logger.info("Fetching overall productivity");
        try {
            UserProductivityDTO overallProductivity = userProductivityService.getOverallProductivity();
            logger.debug("Overall productivity: {}", overallProductivity);
            return ResponseEntity.ok(overallProductivity);
        } catch (Exception e) {
            logger.error("Error fetching overall productivity", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

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

    @GetMapping("/api/user-productivity/{username}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Map<String, Object>> getUserProductivity(@PathVariable String username) {
        logger.info("Fetching user productivity for username: {}", username);
        Map<String, Object> productivity = userService.getUserProductivity(username);
        logger.debug("User productivity for {}: {}", username, productivity);
        return ResponseEntity.ok(productivity);
    }

    @GetMapping(value = "/api/user-productivity-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserProductivity() {
        logger.info("New SSE connection established");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.userProductivityService.addEmitter(emitter);
        emitter.onCompletion(() -> logger.info("SSE connection completed"));
        emitter.onTimeout(() -> logger.info("SSE connection timed out"));
        return emitter;
    }

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
}