package com.ticketsystem.backend.exceptions;

public class InvalidTicketDataException extends RuntimeException {
    public InvalidTicketDataException(String message) {
        super(message);
    }
}