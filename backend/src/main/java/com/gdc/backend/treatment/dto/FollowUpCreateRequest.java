package com.gdc.backend.treatment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record FollowUpCreateRequest(
        @NotBlank(message = "Patient is required")
        String patientId,

        String relatedTreatmentId,

        @NotNull(message = "Follow-up date is required")
        Instant followUpDate,

        @Size(max = 2000, message = "Doctor notes are too long")
        String doctorNotes,

        @Valid
        List<ProblemRequest> newProblems,

        @Valid
        List<PrescriptionItemRequest> prescribedMedicines
) {
}
