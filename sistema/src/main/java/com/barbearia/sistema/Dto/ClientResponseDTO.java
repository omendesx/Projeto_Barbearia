package com.barbearia.sistema.Dto;

import java.time.LocalDateTime;

public record ClientResponseDTO(Long id, String name, String email, Integer age, Boolean active,
                                LocalDateTime dateRegister, LocalDateTime dateUpdate) {
}
