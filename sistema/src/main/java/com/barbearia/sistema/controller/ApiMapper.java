package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.*;
import com.barbearia.sistema.model.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class ApiMapper {
    public ServiceResponseDTO service(BarberService s) {
        return new ServiceResponseDTO(s.getId(), s.getName(), s.getDescription(), s.getPrice(),
                s.getDurationMinutes(), s.getActive(), s.getCreatedAt(), s.getUpdatedAt());
    }

    public ProfessionalResponseDTO professional(Professional p) {
        return new ProfessionalResponseDTO(p.getId(), p.getName(), p.getEmail(), p.getPhone(), p.getActive(),
                p.getServices().stream().sorted(Comparator.comparing(BarberService::getName)).map(this::service).toList(),
                p.getCreatedAt(), p.getUpdatedAt());
    }

    public AppointmentResponseDTO appointment(Appointment a) {
        return new AppointmentResponseDTO(a.getId(), a.getClient().getId(), a.getClient().getName(),
                a.getProfessional().getId(), a.getProfessional().getName(),
                a.getServices().stream().sorted(Comparator.comparing(BarberService::getName)).map(this::service).toList(),
                a.getStartTime(), a.getEndTime(), a.getStatus(), a.getTotalPrice(), a.getNotes(),
                a.getCreatedAt(), a.getUpdatedAt());
    }
}
