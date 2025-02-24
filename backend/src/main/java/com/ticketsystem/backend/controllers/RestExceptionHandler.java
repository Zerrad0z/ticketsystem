package com.ticketsystem.backend.controllers;

import com.ticketsystem.backend.exceptions.InvalidCredentialsException;
import com.ticketsystem.backend.exceptions.InvalidTicketDataException;
import com.ticketsystem.backend.exceptions.TicketNotFoundException;
import com.ticketsystem.backend.exceptions.UnauthorizedAccessException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    protected ResponseEntity<Object> handleTicketNotFound(TicketNotFoundException ex) {
        log.error("Ticket not found: {}", ex.getMessage());
        return new ResponseEntity<>("Ticket not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return new ResponseEntity<>("User not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    protected ResponseEntity<Object> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());
        return new ResponseEntity<>("Unauthorized access: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidTicketDataException.class)
    protected ResponseEntity<Object> handleInvalidTicketData(InvalidTicketDataException ex) {
        log.error("Invalid ticket data: {}", ex.getMessage());
        return new ResponseEntity<>("Invalid ticket data: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    protected ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}