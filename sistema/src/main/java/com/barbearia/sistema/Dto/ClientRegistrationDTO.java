package com.barbearia.sistema.Dto;

import jakarta.validation.constraints.*;

public record ClientRegistrationDTO(
        @NotBlank @Size(max=100) String name,
        @NotBlank @Email @Size(max=150) String email,
        @NotBlank @Size(max=30) String phone,
        @NotBlank @Size(min=6,max=100) String password) {}
