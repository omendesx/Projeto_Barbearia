package com.barbearia.sistema.service;

import com.barbearia.sistema.exception.DuplicateResourceException;
import com.barbearia.sistema.exception.ResourceNotFoundException;
import com.barbearia.sistema.model.User;
import com.barbearia.sistema.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository repository) { this.repository = repository; }

    @Override public List<User> findAll() { return repository.findAll(); }
    @Override public Optional<User> findById(Long id) { return repository.findById(id); }
    @Override public Optional<User> findByEmail(String email) { return repository.findByEmailIgnoreCase(email); }

    @Override
    @Transactional
    public User save(User user) {
        if (repository.existsByEmailIgnoreCase(user.getEmail())) throw new DuplicateResourceException("Email ja cadastrado");
        normalizeAndEncode(user);
        user.setId(null);
        return repository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, User input) {
        User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        if (repository.existsByEmailIgnoreCaseAndIdNot(input.getEmail(), id)) throw new DuplicateResourceException("Email ja cadastrado");
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        user.setPassword(input.getPassword());
        user.setRole(input.getRole());
        normalizeAndEncode(user);
        return repository.save(user);
    }

    private void normalizeAndEncode(User user) {
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setRole(user.getRole().trim().toUpperCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Override public void delete(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        repository.delete(user);
    }
    @Override public boolean existsById(Long id) { return repository.existsById(id); }
    @Override public long countUsers() { return repository.count(); }
    @Override public List<User> findByNameContaining(String name) { return repository.findByNameContainingIgnoreCase(name); }
}
