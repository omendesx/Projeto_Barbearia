package com.barbearia.sistema.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Entidade do profissional e dos serviços que ele está habilitado a executar. */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "professionals")
public class Professional {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false)
    private Boolean active = true;

    // A tabela intermediária guarda os pares profissional-serviço.
    @ManyToMany
    @JoinTable(name = "professional_services",
            joinColumns = @JoinColumn(name = "professional_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<BarberService> services = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Callbacks preenchem a auditoria sem depender do controller.
    @PrePersist void createDates() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate void updateDate() { updatedAt = LocalDateTime.now(); }
}
