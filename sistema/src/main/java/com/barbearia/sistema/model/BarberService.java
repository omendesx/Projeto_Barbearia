package com.barbearia.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entidade que representa um item comercial do catálogo da barbearia. */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "services")
public class BarberService {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // BigDecimal com duas casas evita erros de arredondamento de ponto flutuante.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    // Exclusão lógica: serviços antigos podem ser inativados sem perder o histórico.
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Executados pelo JPA imediatamente antes do INSERT e UPDATE.
    @PrePersist void createDates() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
}
