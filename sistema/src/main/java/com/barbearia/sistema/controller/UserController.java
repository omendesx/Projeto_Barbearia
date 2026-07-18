package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.UserRequestDTO;
import com.barbearia.sistema.Dto.UserResponseDTO;
import com.barbearia.sistema.model.User;
import com.barbearia.sistema.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> list(@RequestParam(required = false) String name) {
        List<User> users = name == null ? userService.findAll() : userService.findByNameContaining(name);
        return users.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {
        return toResponse(userService.findById(id).orElseThrow(
                () -> new com.barbearia.sistema.exception.ResourceNotFoundException("Usuario nao encontrado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserRequestDTO request) {
        return toResponse(userService.save(toEntity(request)));
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO request) {
        return toResponse(userService.update(id, toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    private User toEntity(UserRequestDTO dto) {
        return new User(dto.getName(), dto.getEmail(), dto.getPassword(), dto.getRole());
    }

    private UserResponseDTO toResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
