package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.ImplantType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ImplantDetailRequest(
        ImplantType implantType,

        @DecimalMin(value = "0.00", inclusive = false, message = "Implant width must be greater than zero")
        BigDecimal implantWidthMm,

        @DecimalMin(value = "0.00", inclusive = false, message = "Implant length must be greater than zero")
        BigDecimal implantLengthMm,

        @Size(max = 150, message = "Implant company is too long")
        String implantCompany,

        LocalDate healingCapPlacementDate,
        LocalDate abutmentPlacementDate,
        LocalDate crownPlacementDate
) {
}
