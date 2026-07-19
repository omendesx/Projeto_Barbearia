package com.barbearia.sistema.service;

import com.barbearia.sistema.model.Client;
import java.util.List;

/** Contrato da camada de negócio de clientes, independente da implementação. */
public interface ClientService {
    // Operações de consulta, criação, atualização e exclusão usadas pelo controller.
    List<Client> findAll(String name);
    Client findById(Long id);
    Client create(Client client);
    Client update(Long id, Client client);
    void delete(Long id);
}
