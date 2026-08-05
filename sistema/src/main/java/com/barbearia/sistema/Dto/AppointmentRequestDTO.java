package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

/** Dados aceitos para criar/reagendar; record gera construtor e acessores imutáveis. */
public record AppointmentRequestDTO(
        // @NotNull exige os relacionamentos; @NotEmpty exige ao menos um serviço.
        @NotNull Long clientId,
        @NotNull Long professionalId,
        @NotEmpty Set<@NotNull Long> serviceIds,
        // @Future impede agendamentos no passado; notes aceita no máximo 500 caracteres.
        @NotNull @Future LocalDateTime startTime,
        @Size(max = 500) String notes) {}

