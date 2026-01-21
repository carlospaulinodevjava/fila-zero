package com.filazero.appointmentservice.exception;

public class NurseNotFoundException extends RuntimeException {
    public NurseNotFoundException(Long nurseId) {
      super("Enfermeiro não encontrado com ID: " + nurseId);
    }
}
