package com.barbearia.sistema.Dto;

import com.barbearia.sistema.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusDTO(@NotNull AppointmentStatus status) {}

