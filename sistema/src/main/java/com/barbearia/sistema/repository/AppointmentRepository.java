package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Appointment;
import com.barbearia.sistema.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** Camada de acesso aos agendamentos; JpaRepository fornece o CRUD básico. */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    // Spring Data deriva estas três consultas a partir do nome de cada método.
    List<Appointment> findAllByOrderByStartTimeDesc();
    List<Appointment> findByClientIdOrderByStartTimeDesc(Long clientId);
    List<Appointment> findByProfessionalIdOrderByStartTimeDesc(Long professionalId);

    // JPQL consulta entidades/campos Java, não nomes físicos de tabelas/colunas.
    @Query("""
        select count(a) > 0 from Appointment a
        where a.professional.id = :professionalId
          and a.status not in :ignoredStatuses
          and (:excludedId is null or a.id <> :excludedId)
          and a.startTime < :endTime and a.endTime > :startTime
        """)
    // Dois intervalos colidem quando cada um começa antes do término do outro.
    boolean hasConflict(@Param("professionalId") Long professionalId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("excludedId") Long excludedId,
                        @Param("ignoredStatuses") Collection<AppointmentStatus> ignoredStatuses);
}

