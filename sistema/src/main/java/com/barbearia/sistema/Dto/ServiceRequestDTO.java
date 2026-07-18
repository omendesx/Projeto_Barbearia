package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServiceRequestDTO(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @Min(5) @Max(1440) Integer durationMinutes,
        Boolean active) {}

