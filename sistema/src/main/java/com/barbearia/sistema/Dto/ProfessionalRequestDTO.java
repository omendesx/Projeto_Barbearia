package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;
import java.util.Set;

/** Entrada para criar/editar profissional e vincular os serviços que realiza. */
public record ProfessionalRequestDTO(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @Size(min = 6, max = 100) String password,
        Boolean active,
        // Set elimina IDs repetidos; a anotação interna impede elementos nulos.
        @NotNull Set<@NotNull Long> serviceIds) {}

