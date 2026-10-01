package com.gdc.backend.appointment.dto;

import java.time.LocalTime;

public record DoctorBusySlotResponse(
        Long id,
        LocalTime time,
        String notes
) {
}
