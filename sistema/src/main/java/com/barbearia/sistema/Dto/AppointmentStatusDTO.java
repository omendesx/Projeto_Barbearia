package com.barbearia.sistema.Dto;

import com.barbearia.sistema.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/** Corpo mínimo usado pelo endpoint PATCH que altera somente o status. */
public record AppointmentStatusDTO(@NotNull AppointmentStatus status) {}

