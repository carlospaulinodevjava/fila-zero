package com.filazero.appointmentservice.service.helper;

import com.filazero.appointmentservice.persistence.entity.Doctor;
import com.filazero.appointmentservice.persistence.entity.User;
import com.filazero.appointmentservice.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DoctorStatusManager {

    private final UserRepository userRepository;

    public DoctorStatusManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void changeDoctorStatus(Doctor doctor, boolean enabled) {
        User user = doctor.getUser();
        if (user != null) {
            user.setEnabled(enabled);
            userRepository.save(user);
        }
    }
}
