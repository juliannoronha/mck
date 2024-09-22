package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.services.UserService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ProductivityController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ProductivityController.class);

    public ProductivityController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/overall-productivity")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Cacheable("overallProductivity")
    public ResponseEntity<UserProductivityDTO> getOverallProductivity() {
        logger.info("Fetching overall productivity");
        UserProductivityDTO overallProductivity = userService.getOverallProductivity();
        logger.debug("Overall productivity: {}", overallProductivity);
        return ResponseEntity.ok(overallProductivity);
    }

    @GetMapping("/user-productivity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String userProductivity(Model model, 
                                   @RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching user productivity for all users");
        List<UserProductivityDTO> users = userService.getAllUserProductivity(page, size);
        logger.debug("Retrieved {} user productivity records", users.size());
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
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
}