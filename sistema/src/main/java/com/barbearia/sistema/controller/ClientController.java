package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.ClientRequestDTO;
import com.barbearia.sistema.Dto.ClientResponseDTO;
import com.barbearia.sistema.model.Client;
import com.barbearia.sistema.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) { this.clientService = clientService; }

    @GetMapping
    public List<ClientResponseDTO> list(@RequestParam(required = false) String name) {
        return clientService.findAll(name).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO findById(@PathVariable Long id) { return toResponse(clientService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponseDTO create(@Valid @RequestBody ClientRequestDTO request) {
        return toResponse(clientService.create(toEntity(request)));
    }

    @PutMapping("/{id}")
    public ClientResponseDTO update(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO request) {
        return toResponse(clientService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { clientService.delete(id); }

    private Client toEntity(ClientRequestDTO request) {
        Client client = new Client(request.name(), request.email(), request.age());
        client.setActive(request.active());
        return client;
    }

    private ClientResponseDTO toResponse(Client client) {
        return new ClientResponseDTO(client.getId(), client.getName(), client.getEmail(), client.getAge(),
                client.getActive(), client.getDateRegister(), client.getDateUpdate());
    }
}
