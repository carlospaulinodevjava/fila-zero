package com.filazero.appointmentservice.pagination;

public record PageOutput(int page, int size, int totalPages, Long totalElements) {

}
