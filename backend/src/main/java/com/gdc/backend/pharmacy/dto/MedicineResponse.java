package com.gdc.backend.pharmacy.dto;

import java.time.LocalDate;

public record MedicineResponse(
        String medicineId,
        String medicineName,
        Integer availableQuantity,
        LocalDate expiryDate
) {
}
