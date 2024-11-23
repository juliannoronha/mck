package com.demoproject.demo.connections;

import java.util.logging.Logger;

import org.hibernate.exception.JDBCConnectionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DatabaseExceptionHandler {
    
    private static final Logger logger = Logger.getLogger(DatabaseExceptionHandler.class.getName());
    
    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<String> handleConnectionException(JDBCConnectionException ex) {
        logger.severe("Database connection error: " + ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Database connection error occurred. Please try again later.");
    }

}
