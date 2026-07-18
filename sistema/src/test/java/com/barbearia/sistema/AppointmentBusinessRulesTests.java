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

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rules;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AppointmentBusinessRulesTests {
    @Autowired AppointmentService appointmentService;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired ProfessionalRepository professionalRepository;
    @Autowired BarberServiceRepository serviceRepository;
    @Autowired ClientRepository clientRepository;

    @BeforeEach
    void clean() {
        appointmentRepository.deleteAll();
        professionalRepository.deleteAll();
        serviceRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void calculatesTotalAndDurationAndRejectsOverlappingAppointment() {
        Client firstClient = clientRepository.save(new Client("Cliente 1", "cliente1@test.com", 25));
        Client secondClient = clientRepository.save(new Client("Cliente 2", "cliente2@test.com", 30));

        BarberService haircut = service("Corte", "40.00", 30);
        BarberService beard = service("Barba", "25.00", 20);
        Professional professional = new Professional();
        professional.setName("Profissional");
        professional.setEmail("profissional@test.com");
        professional.setServices(new LinkedHashSet<>(Set.of(haircut, beard)));
        professional = professionalRepository.save(professional);

        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        var created = appointmentService.create(new AppointmentRequestDTO(firstClient.getId(), professional.getId(),
                Set.of(haircut.getId(), beard.getId()), start, null));

        assertThat(created.getTotalPrice()).isEqualByComparingTo("65.00");
        assertThat(created.getEndTime()).isEqualTo(start.plusMinutes(50));

        Long professionalId = professional.getId();
        assertThatThrownBy(() -> appointmentService.create(new AppointmentRequestDTO(secondClient.getId(), professionalId,
                Set.of(haircut.getId()), start.plusMinutes(10), null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("indisponivel");
    }

    private BarberService service(String name, String price, int minutes) {
        BarberService service = new BarberService();
        service.setName(name);
        service.setPrice(new BigDecimal(price));
        service.setDurationMinutes(minutes);
        return serviceRepository.save(service);
    }
}
