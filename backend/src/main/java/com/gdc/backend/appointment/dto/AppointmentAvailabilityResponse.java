package com.gdc.backend.appointment.dto;

import java.time.LocalDate;
import java.util.List;

public record AppointmentAvailabilityResponse(
        LocalDate date,
        List<AppointmentSlotResponse> slots
) {
}
