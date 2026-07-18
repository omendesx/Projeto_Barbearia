package com.barbearia.sistema.service;

import com.barbearia.sistema.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    List<User> findAll();
    
    Optional<User> findById(Long id);
    
    User findByEmail(String email);
    
    User save(User user);
    
    User update(Long id, User userDetails);
    
    void delete(Long id);
    
    boolean existsById(Long id);
    
    long countUsers();
    
    List<User> findByNameContaining(String name);
}