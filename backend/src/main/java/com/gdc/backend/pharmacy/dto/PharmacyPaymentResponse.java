package com.gdc.backend.pharmacy.dto;

import com.gdc.backend.pharmacy.entity.PaymentMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PharmacyPaymentResponse(
        String paymentId,
        String patientId,
        String patientName,
        PaymentMode paymentMode,
        BigDecimal totalAmount,
        String details,
        Instant paidAt,
        List<PharmacyPaymentItemResponse> items,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
