package com.gdc.backend.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorBusyCreateRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Time is required")
        LocalTime time,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
