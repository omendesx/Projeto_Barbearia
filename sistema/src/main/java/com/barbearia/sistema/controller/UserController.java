package com.barbearia.sistema.controller;

import com.barbearia.sistema.model.User;
import com.barbearia.sistema.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Injeção via construtor (não precisa de @Autowired)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> listarTodos() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> buscarPorId(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> criar(@RequestBody User user) {
        User novoUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> atualizar(@PathVariable Long id, @RequestBody User user) {
        try {
            User userAtualizado = userService.update(id, user);
            return ResponseEntity.ok(userAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}