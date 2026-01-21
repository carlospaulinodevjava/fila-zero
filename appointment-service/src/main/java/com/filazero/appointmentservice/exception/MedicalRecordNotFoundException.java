package com.filazero.appointmentservice.exception;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(String message) {
        super(message);
    }
}

