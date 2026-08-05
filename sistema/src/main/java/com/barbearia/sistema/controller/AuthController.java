package com.barbearia.sistema.controller;

import com.barbearia.sistema.Dto.*;
import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.exception.DuplicateResourceException;
import com.barbearia.sistema.model.Client;
import com.barbearia.sistema.model.User;
import com.barbearia.sistema.repository.ClientRepository;
import com.barbearia.sistema.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final ClientRepository clients;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users, ClientRepository clients) { this.users=users; this.clients=clients; }

    @PostMapping("/login")
    public SessionResponseDTO login(@Valid @RequestBody LoginRequestDTO request, HttpSession session) {
        User user=users.findByEmailIgnoreCase(request.email()).orElse(null);
        if(user!=null && encoder.matches(request.password(),user.getPassword()) && "ADMIN".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("role","ADMIN"); session.setAttribute("userId",user.getId());
            return new SessionResponseDTO(user.getId(),user.getName(),user.getEmail(),"ADMIN");
        }
        Client client=clients.findByEmailIgnoreCase(request.email()).orElse(null);
        if(client!=null && Boolean.TRUE.equals(client.getActive()) && client.getPassword()!=null && encoder.matches(request.password(),client.getPassword())) {
            session.setAttribute("role","CLIENT"); session.setAttribute("clientId",client.getId());
            return new SessionResponseDTO(client.getId(),client.getName(),client.getEmail(),"CLIENT");
        }
        throw new BusinessRuleException("Email ou senha invalidos");
    }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public SessionResponseDTO register(@Valid @RequestBody ClientRegistrationDTO request, HttpSession session) {
        if(clients.existsByEmailIgnoreCase(request.email())||users.existsByEmailIgnoreCase(request.email())) throw new DuplicateResourceException("Email ja cadastrado");
        Client client=new Client(request.name().trim(),request.email().trim().toLowerCase(),request.phone().trim());
        client.setActive(true); client.setPassword(encoder.encode(request.password())); clients.save(client);
        session.setAttribute("role","CLIENT"); session.setAttribute("clientId",client.getId());
        return new SessionResponseDTO(client.getId(),client.getName(),client.getEmail(),"CLIENT");
    }

    @GetMapping("/me")
    public SessionResponseDTO me(HttpSession session) {
        String role=(String)session.getAttribute("role");
        if("ADMIN".equals(role)) { User u=users.findById((Long)session.getAttribute("userId")).orElseThrow(); return new SessionResponseDTO(u.getId(),u.getName(),u.getEmail(),role); }
        if("CLIENT".equals(role)) { Client c=clients.findById((Long)session.getAttribute("clientId")).orElseThrow(); return new SessionResponseDTO(c.getId(),c.getName(),c.getEmail(),role); }
        throw new BusinessRuleException("Sessao nao autenticada");
    }

    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) { session.invalidate(); }
}
