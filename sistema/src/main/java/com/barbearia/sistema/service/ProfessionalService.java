package com.barbearia.sistema.service;

import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.exception.DuplicateResourceException;
import com.barbearia.sistema.exception.ResourceNotFoundException;
import com.barbearia.sistema.model.BarberService;
import com.barbearia.sistema.model.Professional;
import com.barbearia.sistema.repository.BarberServiceRepository;
import com.barbearia.sistema.repository.ProfessionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Regras de negócio e persistência dos profissionais. */
@Service
public class ProfessionalService {
    private final ProfessionalRepository repository;
    private final BarberServiceRepository serviceRepository;

    public ProfessionalService(ProfessionalRepository repository, BarberServiceRepository serviceRepository) {
        this.repository = repository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<Professional> list(boolean onlyActive) {
        return onlyActive ? repository.findByActiveTrueOrderByName() : repository.findAll();
    }

    @Transactional(readOnly = true)
    public Professional find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado"));
    }

    @Transactional
    public Professional create(Professional professional, Set<Long> serviceIds) {
        if (repository.existsByEmailIgnoreCase(professional.getEmail())) throw new DuplicateResourceException("Email de profissional ja cadastrado");
        professional.setId(null);
        // IDs recebidos da API são trocados por entidades válidas antes de salvar.
        professional.setServices(loadServices(serviceIds));
        normalize(professional);
        return repository.save(professional);
    }

    @Transactional
    public Professional update(Long id, Professional input, Set<Long> serviceIds) {
        Professional professional = find(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(input.getEmail(), id)) throw new DuplicateResourceException("Email de profissional ja cadastrado");
        professional.setName(input.getName());
        professional.setEmail(input.getEmail());
        professional.setPhone(input.getPhone());
        if (input.getActive() != null) professional.setActive(input.getActive());
        professional.setServices(loadServices(serviceIds));
        normalize(professional);
        return repository.save(professional);
    }

    @Transactional
    public void deactivate(Long id) {
        Professional professional = find(id);
        professional.setActive(false);
        repository.save(professional);
    }

    // Valida em lote a existência e a situação dos serviços selecionados.
    private Set<BarberService> loadServices(Set<Long> ids) {
        List<BarberService> services = serviceRepository.findAllById(ids);
        if (services.size() != ids.size()) throw new ResourceNotFoundException("Um ou mais servicos nao foram encontrados");
        if (services.stream().anyMatch(s -> !Boolean.TRUE.equals(s.getActive()))) throw new BusinessRuleException("Profissional nao pode receber servico inativo");
        // Set representa a associação sem duplicatas; LinkedHashSet preserva a ordem.
        return new LinkedHashSet<>(services);
    }

    // Normalização evita e-mails semanticamente iguais com caixa/espaços diferentes.
    private void normalize(Professional professional) {
        professional.setName(professional.getName().trim());
        professional.setEmail(professional.getEmail().trim().toLowerCase());
        professional.setPhone(professional.getPhone() == null ? null : professional.getPhone().trim());
        if (professional.getActive() == null) professional.setActive(true);
    }
}
