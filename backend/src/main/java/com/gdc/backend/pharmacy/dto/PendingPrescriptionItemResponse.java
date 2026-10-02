package com.gdc.backend.pharmacy.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PendingPrescriptionItemResponse(
        Long prescriptionItemId,
        String sourceType,
        String sourceId,
        Instant sourceDate,
        String medicineId,
        String medicineName,
        Integer prescribedQuantity,
        Integer availableQuantity,
        LocalDate expiryDate,
        boolean expired
) {
}
