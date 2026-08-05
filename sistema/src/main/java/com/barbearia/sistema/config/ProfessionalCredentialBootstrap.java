package com.barbearia.sistema.config;

import com.barbearia.sistema.repository.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Gera uma credencial inicial apenas para profissionais antigos sem senha. */
@Component
public class ProfessionalCredentialBootstrap implements ApplicationRunner {
    private final ProfessionalRepository professionals;
    private final String defaultPassword;

    public ProfessionalCredentialBootstrap(ProfessionalRepository professionals,
            @Value("${app.professional.default-password:barbeiro123}") String defaultPassword) {
        this.professionals = professionals;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (defaultPassword == null || defaultPassword.length() < 6) return;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        professionals.findAll().stream()
                .filter(p -> p.getPassword() == null || p.getPassword().isBlank())
                .forEach(p -> p.setPassword(encoder.encode(defaultPassword)));
    }
}
