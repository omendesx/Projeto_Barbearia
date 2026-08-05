package com.barbearia.sistema.Dto;

import java.time.LocalDateTime;

/** Campos públicos devolvidos ao consumidor da API de clientes. */
public record ClientResponseDTO(Long id, String name, String email, String phone, Boolean active,
                                LocalDateTime dateRegister, LocalDateTime dateUpdate) {
}
