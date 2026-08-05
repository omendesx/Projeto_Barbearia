package com.barbearia.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

// Lombok gera getters, setters e o construtor vazio exigido pelo JPA.
@Getter @Setter @NoArgsConstructor
@Entity // Mapeia objetos desta classe para registros do banco.
// O índice acelera a busca de horários de um profissional.
@Table(name = "appointments", indexes = @Index(name = "idx_appointment_professional_start", columnList = "professional_id,start_time"))
public class Appointment {
    // IDENTITY deixa o banco gerar a chave primária.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos agendamentos podem pertencer ao mesmo cliente; LAZY carrega sob demanda.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    // Um agendamento possui vários serviços e um serviço aparece em vários agendamentos.
    @ManyToMany
    @JoinTable(name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<BarberService> services = new LinkedHashSet<>();

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // STRING grava o nome do enum, que é mais legível e estável que sua posição numérica.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // precision é o total de dígitos e scale reserva dois deles para centavos.
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Callbacks JPA mantêm automaticamente as datas de auditoria.
    @PrePersist void createDates() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
}
