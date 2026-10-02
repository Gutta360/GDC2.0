package com.gdc.backend.payment.dto;

import com.gdc.backend.payment.entity.PaymentMode;
import com.gdc.backend.treatment.entity.TreatmentType;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        String patientId,
        String patientName,
        String treatmentId,
        Instant treatmentDate,
        TreatmentType treatmentType,
        String paymentFor,
        PaymentMode paymentMode,
        BigDecimal amount,
        String details,
        Instant paidAt,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
