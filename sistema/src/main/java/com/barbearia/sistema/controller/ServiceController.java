package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.ServiceRequestDTO;
import com.barbearia.sistema.Dto.ServiceResponseDTO;
import com.barbearia.sistema.model.BarberService;
import com.barbearia.sistema.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints do catálogo de serviços oferecidos pela barbearia. */
@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final CatalogService service;
    private final ApiMapper mapper;

    public ServiceController(CatalogService service, ApiMapper mapper) { this.service = service; this.mapper = mapper; }

    // stream/map converte cada entidade retornada pelo serviço em um DTO.
    @GetMapping public List<ServiceResponseDTO> list(@RequestParam(defaultValue = "true") boolean onlyActive) {
        return service.list(onlyActive).stream().map(mapper::service).toList();
    }
    @GetMapping("/{id}") public ServiceResponseDTO find(@PathVariable Long id) { return mapper.service(service.find(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponseDTO create(@Valid @RequestBody ServiceRequestDTO request) { return mapper.service(service.create(entity(request))); }
    @PutMapping("/{id}") public ServiceResponseDTO update(@PathVariable Long id, @Valid @RequestBody ServiceRequestDTO request) {
        return mapper.service(service.update(id, entity(request)));
    }
    // Retorna 204 porque a operação tem sucesso mas não precisa de corpo na resposta.
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable Long id) { service.deactivate(id); }

    // Copia o contrato de entrada para uma entidade entendida pela camada de negócio.
    private BarberService entity(ServiceRequestDTO r) {
        BarberService s = new BarberService();
        s.setName(r.name()); s.setDescription(r.description()); s.setPrice(r.price());
        s.setDurationMinutes(r.durationMinutes()); s.setActive(r.active());
        return s;
    }
}
