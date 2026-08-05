package com.barbearia.sistema.exception;

/** Indica dados existentes, porém incompatíveis com uma regra do negócio. */
public class BusinessRuleException extends RuntimeException {
    // Repassa a mensagem explicativa para RuntimeException.
    public BusinessRuleException(String message) { super(message); }
}

