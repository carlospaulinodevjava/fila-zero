package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.NotificationResponseRequestDTO;
import com.filazero.appointmentservice.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final NotificationService notificationService;

    public WebhookController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/process-confirmation")
    public ResponseEntity<String> processConfirmation(@ModelAttribute NotificationResponseRequestDTO request, Model model) {

        notificationService.processConfirmation(request.trackingToken());

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Consulta confirmada</title>
                </head>
                <body style="
                    margin: 0;
                    padding: 0;
                    font-family: Arial, sans-serif;
                    background-color: #f2f2f2;
                ">
                
                    <div style="
                        max-width: 420px;
                        margin: 80px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        padding: 32px;
                        text-align: center;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        color: #349eeb;
                    ">
                        <h2 style="margin-top: 0;">
                            Consulta confirmada com sucesso!
                        </h2>
                
                        <p style="margin-top: 16px; font-size: 15px;">
                            Você já pode fechar esta página.
                        </p>
                    </div>
                
                </body>
                </html>
        """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/process-cancellation")
    public ResponseEntity<?> processCancellation(@ModelAttribute NotificationResponseRequestDTO request) {

        notificationService.processCancellation(request.trackingToken());

        String html = """
                <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>Consulta cancelada</title>
                        </head>
                        <body style="
                            margin: 0;
                            padding: 0;
                            font-family: Arial, sans-serif;
                            background-color: #f2f2f2;
                        ">
                
                            <div style="
                                max-width: 420px;
                                margin: 80px auto;
                                background-color: #ffffff;
                                border-radius: 10px;
                                padding: 32px;
                                text-align: center;
                                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                                color: #a81714;
                            ">
                                <h2 style="margin-top: 0;">
                                    Consulta cancelada
                                </h2>
                
                                <p style="margin-top: 16px; font-size: 15px;">
                                    Sua consulta foi cancelada com sucesso.
                                </p>
                
                                <p style="margin-top: 20px; font-size: 14px; color: #4a6fa5;">
                                    Se precisar, você pode agendar uma nova consulta.
                                </p>
                            </div>
                
                        </body>
                        </html>
                
        """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
