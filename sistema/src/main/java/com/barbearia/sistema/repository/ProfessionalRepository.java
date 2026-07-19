package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Professional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositório dos profissionais e suas consultas específicas. */
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    // Carrega os serviços na mesma consulta porque o DTO é montado após o retorno do service.
    @EntityGraph(attributePaths = "services")
    List<Professional> findByActiveTrueOrderByName();

    @Override
    @EntityGraph(attributePaths = "services")
    List<Professional> findAll();

    @Override
    @EntityGraph(attributePaths = "services")
    Optional<Professional> findById(Long id);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

