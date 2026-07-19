package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.ProfessionalRequestDTO;
import com.barbearia.sistema.Dto.ProfessionalResponseDTO;
import com.barbearia.sistema.model.Professional;
import com.barbearia.sistema.service.ProfessionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints responsáveis pelo cadastro e consulta de profissionais. */
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {
    private final ProfessionalService service;
    private final ApiMapper mapper;

    public ProfessionalController(ProfessionalService service, ApiMapper mapper) { this.service = service; this.mapper = mapper; }

    // Por padrão oculta registros inativos; a query string pode solicitar todos.
    @GetMapping public List<ProfessionalResponseDTO> list(@RequestParam(defaultValue = "true") boolean onlyActive) {
        return service.list(onlyActive).stream().map(mapper::professional).toList();
    }
    @GetMapping("/{id}") public ProfessionalResponseDTO find(@PathVariable Long id) { return mapper.professional(service.find(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProfessionalResponseDTO create(@Valid @RequestBody ProfessionalRequestDTO request) {
        return mapper.professional(service.create(entity(request), request.serviceIds()));
    }
    @PutMapping("/{id}") public ProfessionalResponseDTO update(@PathVariable Long id, @Valid @RequestBody ProfessionalRequestDTO request) {
        return mapper.professional(service.update(id, entity(request), request.serviceIds()));
    }
    // O DELETE implementa exclusão lógica: desativa em vez de apagar do banco.
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable Long id) { service.deactivate(id); }

    // Monta a entidade; a associação de serviços será validada na camada de serviço.
    private Professional entity(ProfessionalRequestDTO r) {
        Professional p = new Professional();
        p.setName(r.name()); p.setEmail(r.email()); p.setPhone(r.phone()); p.setActive(r.active());
        return p;
    }
}
