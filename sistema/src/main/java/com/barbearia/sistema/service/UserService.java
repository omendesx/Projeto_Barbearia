package com.barbearia.sistema.service;

import com.barbearia.sistema.model.User;
import java.util.List;
import java.util.Optional;

/** Define as operações de usuário disponíveis para outras camadas. */
public interface UserService {
    
    List<User> findAll();
    
    // Optional representa explicitamente que a busca pode não encontrar resultado.
    Optional<User> findById(Long id);
    
    Optional<User> findByEmail(String email);
    
    User save(User user);
    
    User update(Long id, User userDetails);
    
    void delete(Long id);
    
    boolean existsById(Long id);
    
    long countUsers();
    
    List<User> findByNameContaining(String name);
}
