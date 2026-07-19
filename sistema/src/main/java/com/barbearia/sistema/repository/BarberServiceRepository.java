package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Persiste serviços; Long é o tipo da chave primária da entidade. */
public interface BarberServiceRepository extends JpaRepository<BarberService, Long> {
    // Os nomes declarativos viram SELECT/EXISTS automaticamente no Spring Data.
    List<BarberService> findByActiveTrueOrderByName();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

