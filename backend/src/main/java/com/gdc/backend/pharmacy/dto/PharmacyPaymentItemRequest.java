package com.gdc.backend.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PharmacyPaymentItemRequest(
        @NotNull(message = "Prescription item is required")
        Long prescriptionItemId,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Unit price must have at most 8 integer digits and 2 decimal places")
        BigDecimal unitPrice
) {
}
