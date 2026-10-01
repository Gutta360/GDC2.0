package com.gdc.backend.appointment.dto;

import com.gdc.backend.appointment.entity.AppointmentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentCreateRequest(

        @NotBlank(message = "Patient ID is required")
        @Size(max = 20, message = "Patient ID must not exceed 20 characters")
        String patientId,

        @NotNull(message = "Appointment date is required")
        LocalDate appointmentDate,

        @NotNull(message = "Appointment time is required")
        LocalTime appointmentTime,

        @NotNull(message = "Appointment type is required")
        AppointmentType appointmentType,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @Min(value = 50, message = "Systolic BP must be at least 50")
        @Max(value = 260, message = "Systolic BP must not exceed 260")
        Short systolicBp,

        @Min(value = 30, message = "Diastolic BP must be at least 30")
        @Max(value = 180, message = "Diastolic BP must not exceed 180")
        Short diastolicBp,

        @Min(value = 30, message = "Heart rate must be at least 30")
        @Max(value = 220, message = "Heart rate must not exceed 220")
        Short heartRate,

        @Min(value = 5, message = "Breathing rate must be at least 5")
        @Max(value = 80, message = "Breathing rate must not exceed 80")
        Short breathingRate,

        @DecimalMin(value = "30.00", message = "Height must be at least 30 cm")
        @DecimalMax(value = "250.00", message = "Height must not exceed 250 cm")
        @Digits(integer = 3, fraction = 2, message = "Height can have at most 3 integer digits and 2 decimal places")
        BigDecimal heightCm,

        @DecimalMin(value = "1.00", message = "Weight must be at least 1 kg")
        @DecimalMax(value = "350.00", message = "Weight must not exceed 350 kg")
        @Digits(integer = 3, fraction = 2, message = "Weight can have at most 3 integer digits and 2 decimal places")
        BigDecimal weightKg,

        @DecimalMin(value = "0.00", message = "FBS cannot be negative")
        @DecimalMax(value = "1000.00", message = "FBS must not exceed 1000")
        @Digits(integer = 4, fraction = 2, message = "FBS can have at most 4 integer digits and 2 decimal places")
        BigDecimal fbs,

        @DecimalMin(value = "0.00", message = "RBS cannot be negative")
        @DecimalMax(value = "1000.00", message = "RBS must not exceed 1000")
        @Digits(integer = 4, fraction = 2, message = "RBS can have at most 4 integer digits and 2 decimal places")
        BigDecimal rbs,

        Boolean hasDiabetes,
        Boolean hasHypertension,
        Boolean hasHeartDisease,
        Boolean hasAsthma,
        Boolean hasKidneyDisease,
        Boolean hasLiverDisease,
        Boolean hasThyroidDisorder,
        Boolean hasBleedingDisorders,
        Boolean hasNeurologicalIssues,

        Boolean hasDrugAllergy,
        Boolean hasFoodAllergy,
        Boolean hasLatexAllergy,

        @Size(max = 500, message = "Other allergy notes must not exceed 500 characters")
        String otherAllergyNotes,

        @Size(max = 1000, message = "Past surgical history must not exceed 1000 characters")
        String pastSurgicalHistory,

        Boolean hasRootCanal,
        Boolean hasImplants,
        Boolean hasCrownsOrBridges,
        Boolean hasBraces,
        Boolean hasDentures,

        @Size(max = 1000, message = "Dental complication notes must not exceed 1000 characters")
        String dentalComplicationNotes,

        Boolean consentGiven,

        @Size(max = 255, message = "Consent reference must not exceed 255 characters")
        String consentReference
) {
}
