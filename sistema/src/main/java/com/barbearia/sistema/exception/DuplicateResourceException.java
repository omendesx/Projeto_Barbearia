package com.barbearia.sistema.exception;

/** Sinaliza tentativa de cadastrar um valor que deve ser único. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}
