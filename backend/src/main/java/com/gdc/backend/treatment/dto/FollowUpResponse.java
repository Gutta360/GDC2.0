package com.gdc.backend.treatment.dto;

import java.time.Instant;
import java.util.List;

public record FollowUpResponse(
        String followUpId,
        String patientId,
        String patientName,
        String relatedTreatmentId,
        Instant followUpDate,
        String doctorNotes,
        List<ProblemResponse> newProblems,
        List<PrescriptionItemResponse> prescribedMedicines,
        List<ScanResponse> scans,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
