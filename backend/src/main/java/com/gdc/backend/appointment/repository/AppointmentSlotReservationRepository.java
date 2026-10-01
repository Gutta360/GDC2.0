package com.gdc.backend.appointment.repository;

import com.gdc.backend.appointment.entity.AppointmentSlotReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AppointmentSlotReservationRepository
        extends JpaRepository<AppointmentSlotReservation, Long> {

    List<AppointmentSlotReservation> findBySlotStartGreaterThanEqualAndSlotStartLessThanAndActiveTrue(
            Instant from,
            Instant to
    );
}
