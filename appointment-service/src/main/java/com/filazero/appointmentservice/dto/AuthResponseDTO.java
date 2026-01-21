package com.filazero.appointmentservice.dto;

public record AuthResponseDTO(
        String acessToken,
        String refreshToken
) {
}
