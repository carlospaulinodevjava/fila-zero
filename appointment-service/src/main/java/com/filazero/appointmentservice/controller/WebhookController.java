package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.NotificationResponseRequestDTO;
import com.filazero.appointmentservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final String ADMIN_ROLE = "hasRole('ADMIN')";
    private static final String DOCTOR_ROLE = "hasRole('DOCTOR')";
    private static final String NURSE_ROLE = "hasRole('NURSE')";

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/process-confirmation")
    public ResponseEntity<?> processConfirmation(
            @ModelAttribute NotificationResponseRequestDTO request) {

        notificationService.processConfirmation(request.trackingToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-cancellation")
    public ResponseEntity<?> processCancellation(@ModelAttribute NotificationResponseRequestDTO request) {

        notificationService.processCancellation(request.trackingToken());
        return ResponseEntity.ok().build();
    }
}
