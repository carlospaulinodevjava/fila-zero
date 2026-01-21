package com.filazero.appointmentservice.exception;

public class CRMAlreadyExistsException extends RuntimeException {
    public CRMAlreadyExistsException(String message) {
        super(message);
    }
}
