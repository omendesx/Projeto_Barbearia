package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Entrada validada do catálogo de serviços. */
public record ServiceRequestDTO(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        // BigDecimal e @Digits preservam precisão monetária e limitam casas decimais.
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        // Duração permitida: entre cinco minutos e um dia completo.
        @NotNull @Min(5) @Max(1440) Integer durationMinutes,
        Boolean active) {}

