package com.demoproject.demo.controller;

import com.demoproject.demo.dto.UserProductivityDTO;
import com.demoproject.demo.services.UserService;
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
    public ResponseEntity<UserProductivityDTO> getOverallProductivity() {
        UserProductivityDTO overallProductivity = userService.getOverallProductivity();
        return ResponseEntity.ok(overallProductivity);
    }

    @GetMapping("/user-productivity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public String userProductivity(Model model) {
        List<UserProductivityDTO> users = userService.getAllUserProductivity();
        model.addAttribute("users", users);
        return "user-productivity";
    }

    @GetMapping("/api/user-productivity/{username}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Map<String, Object>> getUserProductivity(@PathVariable String username) {
        Map<String, Object> productivity = userService.getUserProductivity(username);
        return ResponseEntity.ok(productivity);
    }
}