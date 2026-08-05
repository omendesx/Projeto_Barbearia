package com.barbearia.sistema.exception;

/** Sinaliza que o recurso solicitado não foi encontrado no banco. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
