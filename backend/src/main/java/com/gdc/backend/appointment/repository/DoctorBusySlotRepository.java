package com.gdc.backend.appointment.repository;

import com.gdc.backend.appointment.entity.DoctorBusySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DoctorBusySlotRepository extends JpaRepository<DoctorBusySlot, Long> {

    List<DoctorBusySlot> findByBusyDateTimeGreaterThanEqualAndBusyDateTimeLessThanAndActiveTrueOrderByBusyDateTimeAsc(
            Instant from,
            Instant to
    );

    Optional<DoctorBusySlot> findByIdAndActiveTrue(Long id);
}
