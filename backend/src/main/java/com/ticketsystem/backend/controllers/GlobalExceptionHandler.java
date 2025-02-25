package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler that manages application-wide exceptions
 * and provides consistent error responses
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles case when a requested ticket cannot be found
     * @param ex The exception containing the error message
     * @return 404 NOT_FOUND response with error details
     */
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<Object> handleTicketNotFoundException(TicketNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles case when a requested user cannot be found
     * @param ex The exception containing the error message
     * @return 404 NOT_FOUND response with error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles case when a user attempts an unauthorized operation
     * @param ex The exception containing the error message
     * @return 403 FORBIDDEN response with error details
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles case when invalid data is provided for ticket operations
     * @param ex The exception containing the error message
     * @return 400 BAD_REQUEST response with error details
     */
    @ExceptionHandler(InvalidTicketDataException.class)
    public ResponseEntity<Object> handleInvalidTicketDataException(InvalidTicketDataException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for any uncaught exceptions
     * @param ex The unexpected exception
     * @return 500 INTERNAL_SERVER_ERROR response with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
        return createErrorResponse(
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Handles authentication failures due to invalid credentials
     * @param ex The exception with authentication failure details
     * @return 401 UNAUTHORIZED response
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Helper method to create standardized error responses
     * @param message Error message to include in response
     * @param status HTTP status code for the response
     * @return ResponseEntity containing error details and appropriate status
     */
    private ResponseEntity<Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}