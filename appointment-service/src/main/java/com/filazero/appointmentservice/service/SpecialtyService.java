package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.persistence.entity.Specialty;
import com.filazero.appointmentservice.persistence.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public Specialty create(Specialty specialty) {
        return specialtyRepository.save(specialty);
    }

    public List<Specialty> getAll() {
        return specialtyRepository.findAll();
    }

    public Optional<Specialty> getById(Long id) {
        return specialtyRepository.findById(id);
    }

    public Optional<Specialty> getByName(String name) {
        return specialtyRepository.findByName(name);
    }

    public Specialty update(Long id, Specialty specialty) {
        Specialty existing = specialtyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Especialidade não encontrada com ID: " + id));
        
        existing.setName(specialty.getName());
        existing.setDescription(specialty.getDescription());
        existing.setAverageWaitTime(specialty.getAverageWaitTime());
        
        return specialtyRepository.save(existing);
    }

    public void delete(Long id) {
        specialtyRepository.deleteById(id);
    }
}
