package com.gdc.backend.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MedicineStockRequest(
        @NotBlank(message = "Medicine name is required")
        @Size(max = 150, message = "Medicine name is too long")
        String medicineName,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        @Max(value = 1_000_000, message = "Quantity is too large")
        Integer quantity,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate
) {
}
