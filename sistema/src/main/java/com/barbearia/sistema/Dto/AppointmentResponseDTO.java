package com.barbearia.sistema.Dto;

import com.barbearia.sistema.model.AppointmentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Representação completa e somente de saída de um agendamento. */
public record AppointmentResponseDTO(Long id, Long clientId, String clientName,
                                     Long professionalId, String professionalName,
                                     List<ServiceResponseDTO> services,
                                     LocalDateTime startTime, LocalDateTime endTime,
                                     AppointmentStatus status, BigDecimal totalPrice, String notes,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {}

