package com.filazero.appointmentservice.persistence.repository;

import com.filazero.appointmentservice.persistence.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBynameIgnoreCase(String name);

    Optional<Doctor> findByUser_Id(Long userId);

    Optional<Doctor> findByCrm(String crm);

    @Query(value = "SELECT d.* FROM doctors d " +
        "JOIN users u ON u.id = d.user_id " +
        "WHERE (:name IS NULL OR UPPER(d.name) LIKE UPPER(CONCAT('%', :name, '%'))) " +
        "AND (:crm IS NULL OR UPPER(d.crm) LIKE UPPER(CONCAT('%', :crm, '%'))) " +
        "AND (:specialty IS NULL OR EXISTS (" +
        "    SELECT 1 FROM doctor_specialties ds " +
        "    JOIN specialties s ON s.id = ds.specialty_id " +
        "    WHERE ds.doctor_id = d.id " +
        "      AND UPPER(s.name) LIKE UPPER(CONCAT('%', :specialty, '%'))" +
        ")) " +
        "AND u.enabled = true",
        nativeQuery = true)
    List<Doctor> findActiveDoctorsByFilters(@Param("name") String name,
                                            @Param("specialty") String specialty,
                                            @Param("crm") String crm);

    Optional<Doctor> findByUser_UsernameIgnoreCase(String username);
}
