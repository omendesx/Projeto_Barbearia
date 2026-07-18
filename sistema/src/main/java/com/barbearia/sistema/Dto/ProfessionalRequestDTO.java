package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record ProfessionalRequestDTO(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        Boolean active,
        @NotNull Set<@NotNull Long> serviceIds) {}

