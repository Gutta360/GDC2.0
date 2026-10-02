package com.gdc.backend.pharmacy.dto;

import java.math.BigDecimal;

public record PharmacyPaymentItemResponse(
        Long prescriptionItemId,
        String medicineId,
        String medicineName,
        Integer prescribedQuantity,
        Integer dispensedQuantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
