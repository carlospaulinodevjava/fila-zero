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

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<?> createNurse(@RequestParam Long id) {
        service.createConfirmationNotification(id);
        return ResponseEntity.ok().build();
    }
}
