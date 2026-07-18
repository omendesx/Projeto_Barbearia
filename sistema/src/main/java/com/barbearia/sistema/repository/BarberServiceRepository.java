package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BarberServiceRepository extends JpaRepository<BarberService, Long> {
    List<BarberService> findByActiveTrueOrderByName();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

