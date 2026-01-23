package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.NotificationResponseRequestDTO;
import com.filazero.appointmentservice.persistence.entity.Notification;
import com.filazero.appointmentservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/process-confirmation")
    public ResponseEntity<?> processConfirmation(
            @RequestBody NotificationResponseRequestDTO request) {

        notificationService.processConfirmation(request.trackingToken());

        //TODO: pensar o q mandar no futuro
        /*return ResponseEntity.ok(
                new WebhookResponseDTO(
                        "PROCESSED",
                        notification.getAppointment().getId(),
                        "Resposta processada com sucesso"
                )
        );*/

        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-cancellation")
    public ResponseEntity<?> processCancellation(@RequestBody NotificationResponseRequestDTO request) {

        notificationService.processCancellation(request.trackingToken());

        //TODO: pensar o q mandar no futuro
        /*return ResponseEntity.ok(
                new WebhookResponseDTO(
                        "PROCESSED",
                        notification.getAppointment().getId(),
                        "Resposta processada com sucesso"
                )
        );*/

        return ResponseEntity.ok().build();
    }
}
