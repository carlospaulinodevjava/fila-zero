package com.filazero.appointmentservice.service;

import com.filazero.appointmentservice.dto.CreateMedicalRecordRequestDTO;
import com.filazero.appointmentservice.dto.UpdateMedicalRecordRequestDTO;
import com.filazero.appointmentservice.dto.MedicalRecordResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicalRecordService {
    MedicalRecordResponseDTO create(CreateMedicalRecordRequestDTO createMedicalRecordRequestDTO);
    Page<MedicalRecordResponseDTO> getAll(Pageable pageable);
    MedicalRecordResponseDTO getById(Long id);
    MedicalRecordResponseDTO update(Long id, UpdateMedicalRecordRequestDTO updateMedicalRecordRequestDTO);
    void delete(Long id);
    boolean isPatientOwner(String username, Long medicalRecordId);
}
