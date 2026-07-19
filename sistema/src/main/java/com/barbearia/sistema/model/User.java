package com.barbearia.sistema.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/** Usuário administrativo persistido na tabela users. */
@Entity
@Table(name = "users")
public class User  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false, length = 150)
    private String email;
    
    // Armazena o hash BCrypt, nunca a senha em texto puro.
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public User() {
    }
    
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Define a data somente no primeiro INSERT.
    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Entidades são consideradas iguais quando possuem a mesma identidade persistida.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    // Mantém o contrato: objetos iguais devem produzir o mesmo hash.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    // Representação textual útil em logs; a senha é propositalmente omitida.
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
