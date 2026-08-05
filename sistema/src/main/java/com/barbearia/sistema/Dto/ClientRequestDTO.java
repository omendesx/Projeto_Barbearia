package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrato e validações da entrada de clientes. */
public record ClientRequestDTO(
        // @NotBlank rejeita null/vazio; @Size protege o limite da coluna no banco.
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 30) String phone,
        Boolean active) {
}
