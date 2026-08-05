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

/**
 * Centraliza as regras de negócio dos agendamentos.
 * O controller cuida do HTTP; esta classe valida e persiste os dados.
 */
@Service // Registra a classe como um componente de serviço do Spring.
public class AppointmentService {
    // Status que não ocupam horário, pois o atendimento já não acontecerá.
    private static final Set<AppointmentStatus> NON_BLOCKING = EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
    // Dependências são finais para deixar explícito que não mudam após a construção.
    private final AppointmentRepository repository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final BarberServiceRepository serviceRepository;

    // O Spring encontra este único construtor e injeta automaticamente os repositórios.
    public AppointmentService(AppointmentRepository repository, ClientRepository clientRepository,
                              ProfessionalRepository professionalRepository, BarberServiceRepository serviceRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.professionalRepository = professionalRepository;
        this.serviceRepository = serviceRepository;
    }

    // readOnly evita alterações acidentais e permite otimizações na transação.
    @Transactional(readOnly = true)
    public List<Appointment> list(Long clientId, Long professionalId) {
        // A API aceita no máximo um filtro para manter a consulta sem ambiguidades.
        if (clientId != null && professionalId != null) throw new BusinessRuleException("Filtre por cliente ou profissional, nao pelos dois");
        // O nome dos métodos do repository é interpretado pelo Spring Data e vira SQL.
        if (clientId != null) return repository.findByClientIdOrderByStartTimeDesc(clientId);
        if (professionalId != null) return repository.findByProfessionalIdOrderByStartTimeDesc(professionalId);
        return repository.findAllByOrderByStartTimeDesc();
    }

    @Transactional(readOnly = true)
    public Appointment find(Long id) {
        // Optional.orElseThrow converte a ausência no banco em um erro de domínio.
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Agendamento nao encontrado"));
    }

    @Transactional
    public Appointment create(AppointmentRequestDTO request) {
        // A entidade começa vazia e recebe somente valores validados pelo método comum.
        Appointment appointment = new Appointment();
        fillAndValidate(appointment, request, null);
        // Todo agendamento nasce pendente; o cliente não escolhe o status inicial.
        appointment.setStatus(AppointmentStatus.PENDING);
        return repository.save(appointment);
    }

    @Transactional
    public Appointment reschedule(Long id, AppointmentRequestDTO request) {
        Appointment appointment = find(id);
        // Estados finais não podem voltar ao fluxo nem ter o horário modificado.
        if (Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW).contains(appointment.getStatus())) {
            throw new BusinessRuleException("Este agendamento nao pode mais ser alterado");
        }
        fillAndValidate(appointment, request, id);
        return repository.save(appointment);
    }

    @Transactional
    public Appointment changeStatus(Long id, AppointmentStatus next) {
        Appointment appointment = find(id);
        // A máquina de estados impede saltos como PENDING diretamente para COMPLETED.
        if (!allowedTransitions(appointment.getStatus()).contains(next)) {
            throw new BusinessRuleException("Transicao de status invalida: " + appointment.getStatus() + " -> " + next);
        }
        appointment.setStatus(next);
        return repository.save(appointment);
    }

    @Transactional
    public void deleteFinished(Long id) {
        Appointment appointment = find(id);
        if (!Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW).contains(appointment.getStatus())) {
            throw new BusinessRuleException("Somente agendamentos encerrados podem ser excluidos");
        }
        repository.delete(appointment);
    }

    private void fillAndValidate(Appointment appointment, AppointmentRequestDTO request, Long excludedId) {
        // Carrega as relações por ID e falha cedo quando cliente ou profissional não existe.
        Client client = clientRepository.findById(request.clientId()).orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
        // Boolean.TRUE.equals também funciona com Boolean nulo, sem lançar NullPointerException.
        if (!Boolean.TRUE.equals(client.getActive())) throw new BusinessRuleException("Cliente inativo nao pode agendar");
        Professional professional = professionalRepository.findById(request.professionalId()).orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado"));
        if (!Boolean.TRUE.equals(professional.getActive())) throw new BusinessRuleException("Profissional inativo nao pode receber agendamento");

        // Busca todos os serviços em uma consulta e compara quantidades para detectar IDs inválidos.
        List<BarberService> loaded = serviceRepository.findAllById(request.serviceIds());
        if (loaded.size() != request.serviceIds().size()) throw new ResourceNotFoundException("Um ou mais servicos nao foram encontrados");
        if (loaded.stream().anyMatch(s -> !Boolean.TRUE.equals(s.getActive()))) throw new BusinessRuleException("Servico inativo nao pode ser agendado");
        // Transforma os serviços oferecidos em IDs; Set torna containsAll eficiente.
        Set<Long> offered = new HashSet<>(professional.getServices().stream().map(BarberService::getId).toList());
        if (!offered.containsAll(request.serviceIds())) throw new BusinessRuleException("O profissional nao realiza todos os servicos escolhidos");

        // A duração e o preço são calculados no servidor, nunca confiados ao cliente HTTP.
        int duration = loaded.stream().mapToInt(BarberService::getDurationMinutes).sum();
        LocalDateTime end = request.startTime().plusMinutes(duration);
        // excludedId permite reagendar sem o registro colidir com ele mesmo.
        if (repository.hasConflict(professional.getId(), request.startTime(), end, excludedId, NON_BLOCKING)) {
            throw new BusinessRuleException("Profissional indisponivel neste horario");
        }

        appointment.setClient(client);
        appointment.setProfessional(professional);
        // LinkedHashSet elimina repetidos e conserva uma ordem estável de iteração.
        appointment.setServices(new LinkedHashSet<>(loaded));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(end);
        appointment.setTotalPrice(loaded.stream().map(BarberService::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        // Mantém null como null; quando há texto, remove espaços externos.
        appointment.setNotes(request.notes() == null ? null : request.notes().trim());
    }

    private Set<AppointmentStatus> allowedTransitions(AppointmentStatus current) {
        // A expressão switch devolve exatamente os próximos estados aceitos para o atual.
        return switch (current) {
            case PENDING -> EnumSet.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED);
            case CONFIRMED -> EnumSet.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);
            case IN_PROGRESS -> EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED);
            case COMPLETED, CANCELLED, NO_SHOW -> EnumSet.noneOf(AppointmentStatus.class);
        };
    }
}
