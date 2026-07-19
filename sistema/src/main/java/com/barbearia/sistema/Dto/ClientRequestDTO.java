package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrato e validações da entrada de clientes. */
public record ClientRequestDTO(
        // @NotBlank rejeita null/vazio; @Size protege o limite da coluna no banco.
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 150) String email,
        // A idade é opcional, mas quando presente deve estar no intervalo plausível.
        @Min(0) @Max(130) Integer age,
        Boolean active) {
}
