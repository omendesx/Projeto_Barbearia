package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.AppointmentResponseDTO;
import com.barbearia.sistema.Dto.AppointmentStatusDTO;
import com.barbearia.sistema.Dto.ProfessionalResponseDTO;
import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.service.AppointmentService;
import com.barbearia.sistema.service.ProfessionalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/barber")
public class ProfessionalPortalController {
    private final AppointmentService appointments;
    private final ProfessionalService professionals;
    private final ApiMapper mapper;
    public ProfessionalPortalController(AppointmentService appointments, ProfessionalService professionals, ApiMapper mapper) {
        this.appointments=appointments; this.professionals=professionals; this.mapper=mapper;
    }
    private Long professional(HttpSession session) {
        if(!"PROFESSIONAL".equals(session.getAttribute("role"))) throw new BusinessRuleException("Acesso exclusivo para profissionais");
        return (Long)session.getAttribute("professionalId");
    }
    @GetMapping("/profile")
    public ProfessionalResponseDTO profile(HttpSession session) { return mapper.professional(professionals.find(professional(session))); }
    @GetMapping("/appointments")
    public List<AppointmentResponseDTO> appointments(HttpSession session) { return appointments.list(null,professional(session)).stream().map(mapper::appointment).toList(); }
    @PatchMapping("/appointments/{id}/status")
    public AppointmentResponseDTO status(@PathVariable Long id, @Valid @RequestBody AppointmentStatusDTO request, HttpSession session) {
        var appointment=appointments.find(id);
        if(!appointment.getProfessional().getId().equals(professional(session))) throw new BusinessRuleException("Agendamento nao pertence a este profissional");
        return mapper.appointment(appointments.changeStatus(id,request.status()));
    }
    @DeleteMapping("/appointments/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpSession session) {
        var appointment=appointments.find(id);
        if(!appointment.getProfessional().getId().equals(professional(session))) throw new BusinessRuleException("Agendamento nao pertence a este profissional");
        appointments.deleteFinished(id);
    }
}
