package com.barbearia.sistema.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lombok elimina a repetição de getters, setters e construtores simples.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity // Informa ao JPA que a classe é persistível.
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // unique cria também uma proteção de unicidade no próprio banco.
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "date_register", updatable = false)
    private LocalDateTime dateRegister;

    @Column(name = "date_update")
    private LocalDateTime dateUpdate;

    @Column(name = "active")
    private Boolean active = true;

    // Clientes criados pelo portal possuem credencial própria. Registros antigos podem ficar sem senha.
    @Column(name = "password", length = 255)
    private String password;

    // Construtor de conveniência usado pelo controller e pelos testes.
    public Client(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Chamado automaticamente antes da primeira gravação.
    @PrePersist
    public void prePersist() {
        dateRegister = LocalDateTime.now();
        dateUpdate = LocalDateTime.now();
    }

    // Atualiza somente a data de modificação antes de cada UPDATE.
    @PreUpdate
    public void preUpdate() {
        dateUpdate = LocalDateTime.now();
    }
}
