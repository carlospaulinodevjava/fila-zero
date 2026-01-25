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
    public ResponseEntity<Boolean> executeNotificationProcess(String batchType) {
        // TODO adicionar mecanismo para ter apenas um endpoint e executar a batch de acordo com o tipo

        boolean processStarted = true;

        return ResponseEntity.status(HttpStatus.OK).body(processStarted);
    }
}
