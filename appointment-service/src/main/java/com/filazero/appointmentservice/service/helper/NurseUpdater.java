package com.filazero.appointmentservice.service.helper;

import com.filazero.appointmentservice.dto.update.NurseUpdateDTO;
import com.filazero.appointmentservice.persistence.entity.Nurse;
import org.springframework.stereotype.Component;

@Component
public class NurseUpdater extends Updater{

    public void updateNurseFields(Nurse nurse, NurseUpdateDTO request) {
        updateIfValid(request.name(), nurse::setName);
        updateIfValid(request.coren(), nurse::setCoren);
    }
}
