package com.gdc.backend.treatment.dto;

public record PrescriptionItemResponse(
        String medicineId,
        String medicineName,
        Integer quantity,
        Integer availableQuantity
) {
}
