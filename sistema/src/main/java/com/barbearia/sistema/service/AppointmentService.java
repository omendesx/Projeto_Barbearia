package com.barbearia.sistema.service;

import com.barbearia.sistema.Dto.AppointmentRequestDTO;
import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.exception.ResourceNotFoundException;
import com.barbearia.sistema.model.*;
import com.barbearia.sistema.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService {
    private static final Set<AppointmentStatus> NON_BLOCKING = EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
    private final AppointmentRepository repository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final BarberServiceRepository serviceRepository;

    public AppointmentService(AppointmentRepository repository, ClientRepository clientRepository,
                              ProfessionalRepository professionalRepository, BarberServiceRepository serviceRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.professionalRepository = professionalRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> list(Long clientId, Long professionalId) {
        if (clientId != null && professionalId != null) throw new BusinessRuleException("Filtre por cliente ou profissional, nao pelos dois");
        if (clientId != null) return repository.findByClientIdOrderByStartTimeDesc(clientId);
        if (professionalId != null) return repository.findByProfessionalIdOrderByStartTimeDesc(professionalId);
        return repository.findAllByOrderByStartTimeDesc();
    }

    @Transactional(readOnly = true)
    public Appointment find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Agendamento nao encontrado"));
    }

    @Transactional
    public Appointment create(AppointmentRequestDTO request) {
        Appointment appointment = new Appointment();
        fillAndValidate(appointment, request, null);
        appointment.setStatus(AppointmentStatus.PENDING);
        return repository.save(appointment);
    }

    @Transactional
    public Appointment reschedule(Long id, AppointmentRequestDTO request) {
        Appointment appointment = find(id);
        if (Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW).contains(appointment.getStatus())) {
            throw new BusinessRuleException("Este agendamento nao pode mais ser alterado");
        }
        fillAndValidate(appointment, request, id);
        return repository.save(appointment);
    }

    @Transactional
    public Appointment changeStatus(Long id, AppointmentStatus next) {
        Appointment appointment = find(id);
        if (!allowedTransitions(appointment.getStatus()).contains(next)) {
            throw new BusinessRuleException("Transicao de status invalida: " + appointment.getStatus() + " -> " + next);
        }
        appointment.setStatus(next);
        return repository.save(appointment);
    }

    private void fillAndValidate(Appointment appointment, AppointmentRequestDTO request, Long excludedId) {
        Client client = clientRepository.findById(request.clientId()).orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
        if (!Boolean.TRUE.equals(client.getActive())) throw new BusinessRuleException("Cliente inativo nao pode agendar");
        Professional professional = professionalRepository.findById(request.professionalId()).orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado"));
        if (!Boolean.TRUE.equals(professional.getActive())) throw new BusinessRuleException("Profissional inativo nao pode receber agendamento");

        List<BarberService> loaded = serviceRepository.findAllById(request.serviceIds());
        if (loaded.size() != request.serviceIds().size()) throw new ResourceNotFoundException("Um ou mais servicos nao foram encontrados");
        if (loaded.stream().anyMatch(s -> !Boolean.TRUE.equals(s.getActive()))) throw new BusinessRuleException("Servico inativo nao pode ser agendado");
        Set<Long> offered = new HashSet<>(professional.getServices().stream().map(BarberService::getId).toList());
        if (!offered.containsAll(request.serviceIds())) throw new BusinessRuleException("O profissional nao realiza todos os servicos escolhidos");

        int duration = loaded.stream().mapToInt(BarberService::getDurationMinutes).sum();
        LocalDateTime end = request.startTime().plusMinutes(duration);
        if (repository.hasConflict(professional.getId(), request.startTime(), end, excludedId, NON_BLOCKING)) {
            throw new BusinessRuleException("Profissional indisponivel neste horario");
        }

        appointment.setClient(client);
        appointment.setProfessional(professional);
        appointment.setServices(new LinkedHashSet<>(loaded));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(end);
        appointment.setTotalPrice(loaded.stream().map(BarberService::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        appointment.setNotes(request.notes() == null ? null : request.notes().trim());
    }

    private Set<AppointmentStatus> allowedTransitions(AppointmentStatus current) {
        return switch (current) {
            case PENDING -> EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
            case IN_PROGRESS -> EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED);
            case COMPLETED, CANCELLED, NO_SHOW -> EnumSet.noneOf(AppointmentStatus.class);
        };
    }
}
