package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.ImplantType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ImplantDetailResponse(
        ImplantType implantType,
        BigDecimal implantWidthMm,
        BigDecimal implantLengthMm,
        String implantCompany,
        LocalDate healingCapPlacementDate,
        LocalDate abutmentPlacementDate,
        LocalDate crownPlacementDate
) {
}
