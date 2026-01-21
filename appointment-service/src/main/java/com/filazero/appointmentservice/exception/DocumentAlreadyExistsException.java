package com.filazero.appointmentservice.exception;

public class DocumentAlreadyExistsException extends RuntimeException {
    public DocumentAlreadyExistsException(String message) {
        super(message);
    }
}
