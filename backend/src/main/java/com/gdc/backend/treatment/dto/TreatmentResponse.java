package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.TreatmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TreatmentResponse(
        String treatmentId,
        String patientId,
        String patientName,
        Instant treatmentDate,
        TreatmentType treatmentType,
        BigDecimal treatmentAmount,
        String doctorNotes,
        List<ProblemResponse> problems,
        List<PrescriptionItemResponse> prescribedMedicines,
        List<ScanResponse> scans,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
