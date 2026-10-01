package com.gdc.backend.appointment.repository;

import com.gdc.backend.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @EntityGraph(attributePaths = "patient")
    Optional<Appointment> findByAppointmentIdAndActiveTrue(String appointmentId);

    @EntityGraph(attributePaths = "patient")
    List<Appointment> findByAppointmentDateTimeGreaterThanEqualAndAppointmentDateTimeLessThanAndActiveTrueOrderByAppointmentDateTimeAsc(
            Instant from,
            Instant to
    );

    @Query(value = "SELECT nextval('appointment_number_seq')", nativeQuery = true)
    Long getNextAppointmentNumber();

    @Query("""
            SELECT a
            FROM Appointment a
            JOIN FETCH a.patient p
            WHERE a.appointmentDateTime >= :from
              AND a.appointmentDateTime < :to
              AND a.active = true
            ORDER BY a.appointmentDateTime ASC
            """)
    List<Appointment> findActiveInRangeWithPatient(
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
