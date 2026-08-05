package com.barbearia.sistema.model;

/** Estados possíveis do ciclo de vida de um atendimento. */
public enum AppointmentStatus {
    // Pendente -> confirmado -> em andamento -> concluído; cancelado/ausente encerram o fluxo.
    PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
}
