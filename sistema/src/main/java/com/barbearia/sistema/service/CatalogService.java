package com.barbearia.sistema.service;

import com.barbearia.sistema.exception.DuplicateResourceException;
import com.barbearia.sistema.exception.ResourceNotFoundException;
import com.barbearia.sistema.model.BarberService;
import com.barbearia.sistema.repository.BarberServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Aplica as regras do catálogo antes de acessar o repositório. */
@Service
public class CatalogService {
    private final BarberServiceRepository repository;

    public CatalogService(BarberServiceRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<BarberService> list(boolean onlyActive) {
        // O ternário escolhe a consulta conforme a opção enviada pelo controller.
        return onlyActive ? repository.findByActiveTrueOrderByName() : repository.findAll();
    }

    @Transactional(readOnly = true)
    public BarberService find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Servico nao encontrado"));
    }

    @Transactional
    public BarberService create(BarberService service) {
        if (repository.existsByNameIgnoreCase(service.getName())) throw new DuplicateResourceException("Servico com este nome ja existe");
        // Impede que uma entrada de criação force a atualização de um ID existente.
        service.setId(null);
        normalize(service);
        return repository.save(service);
    }

    @Transactional
    public BarberService update(Long id, BarberService input) {
        // Atualiza a entidade gerenciada existente em vez de substituir seu ID e datas.
        BarberService service = find(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(input.getName(), id)) throw new DuplicateResourceException("Servico com este nome ja existe");
        service.setName(input.getName());
        service.setDescription(input.getDescription());
        service.setPrice(input.getPrice());
        service.setDurationMinutes(input.getDurationMinutes());
        if (input.getActive() != null) service.setActive(input.getActive());
        normalize(service);
        return repository.save(service);
    }

    @Transactional
    public void deactivate(Long id) {
        BarberService service = find(id);
        service.setActive(false);
        repository.save(service);
    }

    // Padroniza texto e valor padrão em um único ponto usado por create/update.
    private void normalize(BarberService service) {
        service.setName(service.getName().trim());
        service.setDescription(service.getDescription() == null ? null : service.getDescription().trim());
        if (service.getActive() == null) service.setActive(true);
    }
}
