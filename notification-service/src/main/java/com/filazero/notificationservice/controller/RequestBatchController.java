package com.filazero.notificationservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batches")
public class RequestBatchController {


    @PostMapping("/execute-notification-process")
    public ResponseEntity<Boolean> executeNotificationProcess() {



        boolean processStarted = true; // Suponha que o processo foi iniciado com sucesso

        return ResponseEntity.status(HttpStatus.OK).body(processStarted);

    }
}
