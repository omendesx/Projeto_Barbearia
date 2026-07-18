package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Busca por email (exato)
    Optional<User> findByEmailIgnoreCase(String email);
    
    // Busca por nome (contém)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Busca por role
    List<User> findByRole(String role);
    
    // Verifica se email existe
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
