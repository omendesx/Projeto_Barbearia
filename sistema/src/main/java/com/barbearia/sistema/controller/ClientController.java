package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.ClientRequestDTO;
import com.barbearia.sistema.Dto.ClientResponseDTO;
import com.barbearia.sistema.model.Client;
import com.barbearia.sistema.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Traduz requisições HTTP de clientes em chamadas da camada de serviço. */
@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) { this.clientService = clientService; }

    // O parâmetro name é opcional: ausente lista todos; presente filtra pelo nome.
    @GetMapping
    public List<ClientResponseDTO> list(@RequestParam(required = false) String name) {
        return clientService.findAll(name).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ClientResponseDTO findById(@PathVariable Long id) { return toResponse(clientService.findById(id)); }

    // Desserializa o JSON no DTO, valida os campos e devolve HTTP 201.
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

    // Converte entrada da API em entidade sem permitir que o cliente defina o ID.
    private Client toEntity(ClientRequestDTO request) {
        Client client = new Client(request.name(), request.email(), request.phone());
        client.setActive(request.active());
        return client;
    }

    // DTO de resposta separa o contrato HTTP da estrutura persistida pelo JPA.
    private ClientResponseDTO toResponse(Client client) {
        return new ClientResponseDTO(client.getId(), client.getName(), client.getEmail(), client.getPhone(),
                client.getActive(), client.getDateRegister(), client.getDateUpdate());
    }
}
