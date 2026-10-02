package com.gdc.backend.payment.dto;

import com.gdc.backend.treatment.entity.TreatmentType;

import java.math.BigDecimal;
import java.time.Instant;

public record OutstandingTreatmentPaymentResponse(
        String treatmentId,
        Instant treatmentDate,
        TreatmentType treatmentType,
        BigDecimal treatmentAmount,
        BigDecimal outstandingAmount
) {
}
