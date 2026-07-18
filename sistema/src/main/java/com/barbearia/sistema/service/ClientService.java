package com.barbearia.sistema.service;

import com.barbearia.sistema.model.Client;
import java.util.List;

public interface ClientService {
    List<Client> findAll(String name);
    Client findById(Long id);
    Client create(Client client);
    Client update(Long id, Client client);
    void delete(Long id);
}
