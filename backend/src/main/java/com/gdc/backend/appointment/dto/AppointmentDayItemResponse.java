package com.gdc.backend.appointment.dto;

import com.gdc.backend.appointment.entity.AppointmentType;

import java.time.LocalTime;

public record AppointmentDayItemResponse(
        String appointmentId,
        LocalTime appointmentTime,
        AppointmentType appointmentType,
        String patientId,
        String patientName,
        String patientMobile,
        String notes
) {
}
