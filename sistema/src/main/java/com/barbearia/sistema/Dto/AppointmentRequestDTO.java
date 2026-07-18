package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Set;

public record AppointmentRequestDTO(
        @NotNull Long clientId,
        @NotNull Long professionalId,
        @NotEmpty Set<@NotNull Long> serviceIds,
        @NotNull @Future LocalDateTime startTime,
        @Size(max = 500) String notes) {}

