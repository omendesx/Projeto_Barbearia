package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    List<Professional> findByActiveTrueOrderByName();
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

