package com.gdc.backend.appointment.dto;

import java.time.LocalDate;

public record AppointmentCalendarDayResponse(
        LocalDate date,
        long newAppointments,
        long followUpAppointments,
        long doctorBusySlots
) {
}
