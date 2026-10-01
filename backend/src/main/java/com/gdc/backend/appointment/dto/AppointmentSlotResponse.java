package com.gdc.backend.appointment.dto;

import java.time.LocalTime;

public record AppointmentSlotResponse(
        LocalTime time,
        boolean available,
        String occupiedBy
) {
}
