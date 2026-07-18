package com.barbearia.sistema.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceResponseDTO(Long id, String name, String description, BigDecimal price,
                                 Integer durationMinutes, Boolean active,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {}

