package com.filazero.appointmentservice.controller;

import com.filazero.appointmentservice.dto.SpecialtyRequestDTO;
import com.filazero.appointmentservice.dto.SpecialtyResponseDTO;
import com.filazero.appointmentservice.persistence.entity.Specialty;
import com.filazero.appointmentservice.service.SpecialtyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<SpecialtyResponseDTO> create(@Valid @RequestBody SpecialtyRequestDTO request) {
        Specialty specialty = new Specialty();
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setAverageWaitTime(request.averageWaitTime());

        Specialty created = specialtyService.create(specialty);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(created));
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<List<SpecialtyResponseDTO>> getAll() {
        List<SpecialtyResponseDTO> specialties = specialtyService.getAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(specialties);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<SpecialtyResponseDTO> getById(@PathVariable Long id) {
        return specialtyService.getById(id)
            .map(specialty -> ResponseEntity.ok(toResponseDTO(specialty)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<SpecialtyResponseDTO> getByName(@RequestParam String name) {
        return specialtyService.getByName(name)
            .map(specialty -> ResponseEntity.ok(toResponseDTO(specialty)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<SpecialtyResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyRequestDTO request) {
        Specialty specialty = new Specialty();
        specialty.setName(request.name());
        specialty.setDescription(request.description());
        specialty.setAverageWaitTime(request.averageWaitTime());

        Specialty updated = specialtyService.update(id, specialty);
        return ResponseEntity.ok(toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        specialtyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private SpecialtyResponseDTO toResponseDTO(Specialty specialty) {
        return new SpecialtyResponseDTO(
            specialty.getId(),
            specialty.getName(),
            specialty.getDescription(),
            specialty.getAverageWaitTime()
        );
    }
}
