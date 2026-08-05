package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.*;
import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.model.*;
import com.barbearia.sistema.repository.*;
import com.barbearia.sistema.service.AppointmentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api/portal")
public class ClientPortalController {
    private static final Set<AppointmentStatus> FREE=EnumSet.of(AppointmentStatus.CANCELLED,AppointmentStatus.NO_SHOW);
    private final AppointmentService appointments; private final AppointmentRepository appointmentRepository;
    private final ProfessionalRepository professionals; private final BarberServiceRepository services; private final ApiMapper mapper;
    public ClientPortalController(AppointmentService appointments,AppointmentRepository appointmentRepository,ProfessionalRepository professionals,BarberServiceRepository services,ApiMapper mapper){this.appointments=appointments;this.appointmentRepository=appointmentRepository;this.professionals=professionals;this.services=services;this.mapper=mapper;}

    private Long client(HttpSession session){if(!"CLIENT".equals(session.getAttribute("role")))throw new BusinessRuleException("Entre em sua conta para continuar");return (Long)session.getAttribute("clientId");}
    @GetMapping("/professionals") @Transactional(readOnly=true)
    public List<ProfessionalResponseDTO> professionals(){return professionals.findAll().stream().filter(p->Boolean.TRUE.equals(p.getActive())).map(mapper::professional).toList();}
    @GetMapping("/services") @Transactional(readOnly=true)
    public List<ServiceResponseDTO> services(){return services.findAll().stream().filter(s->Boolean.TRUE.equals(s.getActive())).map(mapper::service).toList();}
    @GetMapping("/appointments")
    public List<AppointmentResponseDTO> mine(HttpSession session){return appointments.list(client(session),null).stream().map(mapper::appointment).toList();}
    @PostMapping("/appointments") @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO create(@Valid @RequestBody AppointmentRequestDTO input,HttpSession session){var own=new AppointmentRequestDTO(client(session),input.professionalId(),input.serviceIds(),input.startTime(),input.notes());return mapper.appointment(appointments.create(own));}
    @PatchMapping("/appointments/{id}/cancel")
    public AppointmentResponseDTO cancel(@PathVariable Long id,HttpSession session){var found=appointments.find(id);if(!found.getClient().getId().equals(client(session)))throw new BusinessRuleException("Agendamento nao pertence a este cliente");return mapper.appointment(appointments.changeStatus(id,AppointmentStatus.CANCELLED));}

    @GetMapping("/availability") @Transactional(readOnly=true)
    public List<LocalDateTime> availability(@RequestParam Long professionalId,@RequestParam List<Long> serviceIds,@RequestParam LocalDate date,HttpSession session){
        client(session); var professional=professionals.findById(professionalId).orElseThrow(); var selected=services.findAllById(serviceIds);
        if(selected.size()!=new HashSet<>(serviceIds).size()||selected.isEmpty())throw new BusinessRuleException("Selecione servicos validos");
        var offered=new HashSet<>(professional.getServices().stream().map(BarberService::getId).toList());if(!offered.containsAll(serviceIds))throw new BusinessRuleException("O profissional nao realiza os servicos escolhidos");
        int duration=selected.stream().mapToInt(BarberService::getDurationMinutes).sum();List<LocalDateTime> result=new ArrayList<>();
        LocalDateTime start=date.atTime(9,0),close=date.atTime(19,0),now=LocalDateTime.now();
        while(!start.plusMinutes(duration).isAfter(close)){if(start.isAfter(now)&&!appointmentRepository.hasConflict(professionalId,start,start.plusMinutes(duration),null,FREE))result.add(start);start=start.plusMinutes(30);}return result;
    }
}
