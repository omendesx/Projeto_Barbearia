package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.*;
import com.barbearia.sistema.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;
    private final ApiMapper mapper;

    public AppointmentController(AppointmentService service, ApiMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping
    public List<AppointmentResponseDTO> list(@RequestParam(required = false) Long clientId,
                                             @RequestParam(required = false) Long professionalId) {
        return service.list(clientId, professionalId).stream().map(mapper::appointment).toList();
    }
    @GetMapping("/{id}") public AppointmentResponseDTO find(@PathVariable Long id) { return mapper.appointment(service.find(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponseDTO create(@Valid @RequestBody AppointmentRequestDTO request) {
        return mapper.appointment(service.create(request));
    }
    @PutMapping("/{id}") public AppointmentResponseDTO reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDTO request) {
        return mapper.appointment(service.reschedule(id, request));
    }
    @PatchMapping("/{id}/status") public AppointmentResponseDTO status(@PathVariable Long id, @Valid @RequestBody AppointmentStatusDTO request) {
        return mapper.appointment(service.changeStatus(id, request.status()));
    }
}
