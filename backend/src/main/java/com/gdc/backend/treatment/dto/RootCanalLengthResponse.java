package com.gdc.backend.treatment.dto;

import java.math.BigDecimal;

public record RootCanalLengthResponse(
        Integer toothNumber,
        String canalName,
        BigDecimal lengthMm,
        String notes
) {
}
