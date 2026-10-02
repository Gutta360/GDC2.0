package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.TreatmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TreatmentCreateRequest(
        @NotBlank(message = "Patient is required")
        String patientId,

        @NotNull(message = "Treatment date is required")
        Instant treatmentDate,

        @NotNull(message = "Treatment type is required")
        TreatmentType treatmentType,

        @NotNull(message = "Treatment amount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Treatment amount cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Treatment amount must have at most 8 integer digits and 2 decimal places")
        BigDecimal treatmentAmount,

        @Size(max = 2000, message = "Doctor notes are too long")
        String doctorNotes,

        @Valid
        List<ProblemRequest> problems,

        @Valid
        List<PrescriptionItemRequest> prescribedMedicines
) {
}
