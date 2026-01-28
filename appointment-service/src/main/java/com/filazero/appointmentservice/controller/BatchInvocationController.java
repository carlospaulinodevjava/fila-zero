package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.service.ScheduledNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.servlet.function.ServerResponse.status;

@RestController
@RequestMapping("/batches")
public class BatchInvocationController {

    private ScheduledNotificationService scheduledNotificationService;

    public BatchInvocationController(ScheduledNotificationService scheduledNotificationService) {
        this.scheduledNotificationService = scheduledNotificationService;
    }

    @PostMapping("/execute-notification-process")
    public ResponseEntity<Boolean> executeNotificationProcess() {
        boolean processStarted = true;
        scheduledNotificationService.sendConfirmationNotifications();
        return  ResponseEntity.status(HttpStatus.OK).body(processStarted);

    }

    @PostMapping("/execute-expiration-process")
    public ResponseEntity<Boolean> executeExpirationProcess() {

        boolean processStarted = true;
        scheduledNotificationService.processExpiredConfirmations();
        return  ResponseEntity.status(HttpStatus.OK).body(processStarted);
    }

    @PostMapping("/execute-notification-process")
    public ResponseEntity<Boolean> executeReallocateProcess() {

        boolean processStarted = true;
        scheduledNotificationService.processOpenSlotsAndOfferReallocation();

        return ResponseEntity.status(HttpStatus.OK).body(processStarted);
    }
}
