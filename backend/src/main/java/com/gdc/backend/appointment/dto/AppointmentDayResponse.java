package com.gdc.backend.appointment.dto;

import java.time.LocalDate;
import java.util.List;

public record AppointmentDayResponse(
        LocalDate date,
        List<AppointmentDayItemResponse> appointments,
        List<DoctorBusySlotResponse> doctorBusySlots
) {
}
