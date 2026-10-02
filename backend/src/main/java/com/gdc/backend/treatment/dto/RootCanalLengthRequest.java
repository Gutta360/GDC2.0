package com.gdc.backend.treatment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RootCanalLengthRequest(
        @Min(value = 11, message = "Invalid tooth number")
        @Max(value = 48, message = "Invalid tooth number")
        Integer toothNumber,

        @NotBlank(message = "Canal name is required")
        @Size(max = 40, message = "Canal name is too long")
        String canalName,

        @DecimalMin(value = "0.00", inclusive = false, message = "Canal length must be greater than zero")
        BigDecimal lengthMm,

        @Size(max = 500, message = "Canal notes are too long")
        String notes
) {
}
