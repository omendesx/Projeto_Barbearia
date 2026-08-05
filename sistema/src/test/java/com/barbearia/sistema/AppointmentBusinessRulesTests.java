package com.barbearia.sistema;

import com.barbearia.sistema.Dto.AppointmentRequestDTO;
import com.barbearia.sistema.exception.BusinessRuleException;
import com.barbearia.sistema.model.BarberService;
import com.barbearia.sistema.model.Client;
import com.barbearia.sistema.model.Professional;
import com.barbearia.sistema.repository.AppointmentRepository;
import com.barbearia.sistema.repository.BarberServiceRepository;
import com.barbearia.sistema.repository.ClientRepository;
import com.barbearia.sistema.repository.ProfessionalRepository;
import com.barbearia.sistema.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Usa H2 em memória e PostgreSQL mode para testar sem tocar no banco real.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rules;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AppointmentBusinessRulesTests {
    // O Spring injeta o serviço real e os repositórios usados na integração.
    @Autowired AppointmentService appointmentService;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired ProfessionalRepository professionalRepository;
    @Autowired BarberServiceRepository serviceRepository;
    @Autowired ClientRepository clientRepository;

    // Garante isolamento: cada teste começa com as tabelas vazias.
    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        professionalRepository.deleteAll();
        serviceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    // Verifica cálculo agregado e a principal regra de conflito de horários.
    @Test
    void calculatesTotalAndDurationAndRejectsOverlappingAppointment() {
        Client firstClient = clientRepository.save(new Client("Cliente 1", "cliente1@test.com", "11999990001"));
        Client secondClient = clientRepository.save(new Client("Cliente 2", "cliente2@test.com", "11999990002"));

        BarberService haircut = service("Corte", "40.00", 30);
        BarberService beard = service("Barba", "25.00", 20);
        Professional professional = new Professional();
        professional.setName("Profissional");
        professional.setEmail("profissional@test.com");
        professional.setServices(new LinkedHashSet<>(Set.of(haircut, beard)));
        professional = professionalRepository.save(professional);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        // Cria um agendamento com dois serviços: 40+25 reais e 30+20 minutos.
        var created = appointmentService.create(new AppointmentRequestDTO(firstClient.getId(), professional.getId(),
                Set.of(haircut.getId(), beard.getId()), start, null));

        assertThat(created.getTotalPrice()).isEqualByComparingTo("65.00");
        assertThat(created.getEndTime()).isEqualTo(start.plusMinutes(50));

        Long professionalId = professional.getId();
        // O segundo horário começa dentro do primeiro e deve lançar a exceção esperada.
        assertThatThrownBy(() -> appointmentService.create(new AppointmentRequestDTO(secondClient.getId(), professionalId,
                Set.of(haircut.getId()), start.plusMinutes(10), null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("indisponivel");
    }

    // Helper reduz repetição ao preparar serviços persistidos para o cenário.
    private BarberService service(String name, String price, int minutes) {
        BarberService service = new BarberService();
        service.setName(name);
        service.setPrice(new BigDecimal(price));
        service.setDurationMinutes(minutes);
        return serviceRepository.save(service);
    }
}
