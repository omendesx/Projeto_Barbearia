package com.barbearia.sistema.Dto;

import java.time.LocalDateTime;
import java.util.List;

/** Saída do profissional, incluindo seus serviços já convertidos em DTOs. */
public record ProfessionalResponseDTO(Long id, String name, String email, String phone, Boolean active,
                                      List<ServiceResponseDTO> services,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {}

