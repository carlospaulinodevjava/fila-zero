package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final String ADMIN_ROLE = "hasRole('ADMIN')";
    private static final String DOCTOR_ROLE = "hasRole('DOCTOR')";
    private static final String NURSE_ROLE = "hasRole('NURSE')";

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }


    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize(DOCTOR_ROLE + " or " + NURSE_ROLE + " or " + ADMIN_ROLE)
    public ResponseEntity<?> createConfirmationNotification(@RequestParam Long id) {
        service.createConfirmationNotification(id);
        return ResponseEntity.ok().build();
    }
}
