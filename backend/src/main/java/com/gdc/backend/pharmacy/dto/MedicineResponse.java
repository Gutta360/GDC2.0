package com.gdc.backend.pharmacy.dto;

public record MedicineResponse(
        String medicineId,
        String medicineName,
        Integer availableQuantity
) {
}
