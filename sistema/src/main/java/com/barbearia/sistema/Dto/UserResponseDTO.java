package com.barbearia.sistema.Dto;

import lombok.Data;
import java.time.LocalDateTime;

/** Resposta do usuário; não contém o campo password por segurança. */
@Data // Lombok gera os métodos de acesso usados pelo controller.
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
