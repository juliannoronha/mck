package com.demoproject.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api")
public class NewRolesController {

    private static final Logger logger = LoggerFactory.getLogger(NewRolesController.class);

    @GetMapping("/checker/tasks")
    @PreAuthorize("hasRole('CHECKER')")
    public ResponseEntity<?> getCheckerTasks(Authentication authentication) {
        logger.info("User {} accessed checker tasks", authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checker/complete-task")
    @PreAuthorize("hasRole('CHECKER')")
    public ResponseEntity<?> completeCheckerTask(@RequestBody String task, Authentication authentication) {
        logger.info("User {} completed checker task: {}", authentication.getName(), task);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shipping/orders")
    @PreAuthorize("hasRole('SHIPPING')")
    public ResponseEntity<?> getShippingOrders(Authentication authentication) {
        logger.info("User {} accessed shipping orders", authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shipping/process-order")
    @PreAuthorize("hasRole('SHIPPING')")
    public ResponseEntity<?> processShippingOrder(@RequestBody String orderNumber, Authentication authentication) {
        logger.info("User {} processed shipping order: {}", authentication.getName(), orderNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inventory/stock")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<?> getInventoryStock(Authentication authentication) {
        logger.info("User {} accessed inventory stock", authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/update-stock")
    @PreAuthorize("hasRole('INVENTORY')")
    public ResponseEntity<?> updateInventoryStock(@RequestBody String item, @RequestBody int quantity, Authentication authentication) {
        logger.info("User {} updated inventory stock for item: {} with quantity: {}", authentication.getName(), item, quantity);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("An error occurred: ", e);
        return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
    }
}