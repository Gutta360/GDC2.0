package com.gdc.backend.appointment.dto;

import com.gdc.backend.appointment.entity.AppointmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        String appointmentId,
        String patientId,
        String patientName,
        String patientMobile,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        Instant appointmentDateTime,
        AppointmentType appointmentType,
        String notes,
        Short systolicBp,
        Short diastolicBp,
        Short heartRate,
        Short breathingRate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bmi,
        BigDecimal fbs,
        BigDecimal rbs,
        boolean hasDiabetes,
        boolean hasHypertension,
        boolean hasHeartDisease,
        boolean hasAsthma,
        boolean hasKidneyDisease,
        boolean hasLiverDisease,
        boolean hasThyroidDisorder,
        boolean hasBleedingDisorders,
        boolean hasNeurologicalIssues,
        boolean hasDrugAllergy,
        boolean hasFoodAllergy,
        boolean hasLatexAllergy,
        String otherAllergyNotes,
        String pastSurgicalHistory,
        boolean hasRootCanal,
        boolean hasImplants,
        boolean hasCrownsOrBridges,
        boolean hasBraces,
        boolean hasDentures,
        String dentalComplicationNotes,
        boolean consentGiven,
        String consentReference,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
