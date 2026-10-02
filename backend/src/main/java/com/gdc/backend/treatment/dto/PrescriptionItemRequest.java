package com.gdc.backend.treatment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrescriptionItemRequest(
        @NotBlank(message = "Medicine ID is required")
        String medicineId,

        @NotNull(message = "Medicine quantity is required")
        @Positive(message = "Medicine quantity must be at least 1")
        Integer quantity
) {
}
