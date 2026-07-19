package com.barbearia.sistema.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Converte exceções Java em respostas HTTP previsíveis para toda a API. */
@RestControllerAdvice // Aplica os handlers a todos os @RestController.
public class GlobalExceptionHandler {
    // Um recurso inexistente corresponde ao status HTTP 404.
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    // Duplicidade conflita com um recurso que já existe: HTTP 409.
    @ExceptionHandler(DuplicateResourceException.class)
    ResponseEntity<ApiError> conflict(DuplicateResourceException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    // HTTP 422 indica JSON válido que não pode ser processado pela regra de negócio.
    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ApiError> businessRule(BusinessRuleException ex, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // LinkedHashMap conserva a ordem; putIfAbsent mantém a primeira mensagem por campo.
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Dados invalidos", request, fields);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request,
                                           Map<String, String> fields) {
        // Monta um único formato de erro e o associa ao status HTTP escolhido.
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
