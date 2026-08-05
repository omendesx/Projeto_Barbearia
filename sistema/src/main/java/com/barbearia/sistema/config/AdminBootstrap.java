package com.barbearia.sistema.config;

import com.barbearia.sistema.model.User;
import com.barbearia.sistema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria o primeiro administrador somente quando credenciais foram fornecidas pelo ambiente. */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final String email, password, name;
    public AdminBootstrap(UserRepository users,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.name:Administrador}") String name) {
        this.users=users; this.email=email; this.password=password; this.name=name;
    }
    @Override public void run(ApplicationArguments args) {
        if(email.isBlank()||password.isBlank()) return;
        User admin=users.findByEmailIgnoreCase(email).orElseGet(User::new);
        admin.setName(name);
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(new BCryptPasswordEncoder().encode(password));
        admin.setRole("ADMIN");
        users.save(admin);
    }
}
