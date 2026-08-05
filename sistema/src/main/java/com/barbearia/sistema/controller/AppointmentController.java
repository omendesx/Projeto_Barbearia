package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.*;
import com.barbearia.sistema.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expõe os casos de uso de agendamento como endpoints HTTP. */
@RestController // O retorno dos métodos é serializado como JSON.
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final ApiMapper mapper;

    // Injeção por construtor deixa as dependências obrigatórias e facilita testes.
    public AppointmentController(AppointmentService service, ApiMapper mapper) { this.service = service; this.mapper = mapper; }

    // GET /api/appointments aceita um dos filtros opcionais da query string.
    @GetMapping
    public List<AppointmentResponseDTO> list(@RequestParam(required = false) Long clientId,
                                             @RequestParam(required = false) Long professionalId) {
        return service.list(clientId, professionalId).stream().map(mapper::appointment).toList();
    }
    // @PathVariable lê o id colocado no próprio caminho da URL.
    @GetMapping("/{id}") public AppointmentResponseDTO find(@PathVariable Long id) { return mapper.appointment(service.find(id)); }
    // POST cria um recurso; 201 CREATED comunica isso corretamente ao cliente.
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO create(@Valid @RequestBody AppointmentRequestDTO request) {
        return mapper.appointment(service.create(request));
    }
    // @Valid executa as restrições declaradas no DTO antes de chamar o serviço.
    @PutMapping("/{id}") public AppointmentResponseDTO reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDTO request) {
        return mapper.appointment(service.reschedule(id, request));
    }
    // PATCH altera somente o status, sem substituir todo o agendamento.
    @PatchMapping("/{id}/status") public AppointmentResponseDTO status(@PathVariable Long id, @Valid @RequestBody AppointmentStatusDTO request) {
        return mapper.appointment(service.changeStatus(id, request.status()));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.deleteFinished(id); }
}
