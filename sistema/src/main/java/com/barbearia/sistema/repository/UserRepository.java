package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Busca por email (exato)
    User findByEmail(String email);
    
    // Busca por nome (contém)
    List<User> findByNameContaining(String name);
    
    // Busca por role
    List<User> findByRole(String role);
    
    // Verifica se email existe
    boolean existsByEmail(String email);
}