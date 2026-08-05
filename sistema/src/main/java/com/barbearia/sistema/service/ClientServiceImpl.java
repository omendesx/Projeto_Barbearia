package com.barbearia.sistema.service;

import com.barbearia.sistema.exception.DuplicateResourceException;
import com.barbearia.sistema.exception.ResourceNotFoundException;
import com.barbearia.sistema.model.Client;
import com.barbearia.sistema.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Implementação concreta do contrato ClientService. */
@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repository;

    // Injeção pelo construtor dispensa @Autowired quando há apenas um construtor.
    public ClientServiceImpl(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll(String name) {
        // null ou texto vazio significam ausência de filtro.
        return name == null || name.isBlank() ? repository.findAll() : repository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));
    }

    @Override
    @Transactional
    public Client create(Client client) {
        if (repository.existsByEmailIgnoreCase(client.getEmail())) {
            throw new DuplicateResourceException("Email ja cadastrado");
        }
        // ID nulo informa ao JPA que deve executar INSERT.
        client.setId(null);
        client.setEmail(client.getEmail().trim().toLowerCase());
        client.setName(client.getName().trim());
        if (client.getActive() == null) client.setActive(true);
        return repository.save(client);
    }

    @Override
    @Transactional
    public Client update(Long id, Client input) {
        Client client = findById(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(input.getEmail(), id)) {
            throw new DuplicateResourceException("Email ja cadastrado");
        }
        client.setName(input.getName().trim());
        client.setEmail(input.getEmail().trim().toLowerCase());
        client.setPhone(input.getPhone() == null ? null : input.getPhone().trim());
        // Se active não veio, conserva o valor que já estava persistido.
        client.setActive(input.getActive() == null ? client.getActive() : input.getActive());
        return repository.save(client);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Client client = findById(id);
        client.setActive(false);
        repository.save(client);
    }
}
