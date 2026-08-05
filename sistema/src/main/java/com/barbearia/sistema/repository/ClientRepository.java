package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositório JPA de clientes, com consultas adicionais derivadas pelo nome. */
public interface ClientRepository extends JpaRepository<Client, Long> {
    // IgnoreCase torna as buscas de nome e e-mail insensíveis a maiúsculas.
    List<Client> findByNameContainingIgnoreCase(String name);
    boolean existsByEmailIgnoreCase(String email);
    // Ao editar, ignora o próprio registro na verificação de e-mail duplicado.
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    Optional<Client> findByEmailIgnoreCase(String email);
}
