package com.barbearia.sistema.Dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok @Data gera getters, setters, equals, hashCode e toString.
@Data
/** Entrada validada para criar ou atualizar um usuário. */
public class UserRequestDTO {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    // Impõe comprimento mínimo antes de a senha ser transformada em hash.
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String role;
}
