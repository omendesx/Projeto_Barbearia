package com.barbearia.sistema.exception;

import java.time.Instant;
import java.util.Map;

/** Formato uniforme do JSON devolvido quando uma requisição falha. */
public record ApiError(Instant timestamp, int status, String error, String message, String path,
                       // fields associa o nome de cada campo inválido à sua mensagem.
                       Map<String, String> fields) {
}
