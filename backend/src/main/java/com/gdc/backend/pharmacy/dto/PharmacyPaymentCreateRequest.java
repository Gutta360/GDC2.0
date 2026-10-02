package com.gdc.backend.pharmacy.dto;

import com.gdc.backend.pharmacy.entity.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PharmacyPaymentCreateRequest(
        @NotBlank(message = "Patient is required")
        String patientId,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode,

        @Size(max = 1000, message = "Payment details are too long")
        String details,

        @NotEmpty(message = "Medicine cart cannot be empty")
        @Valid
        List<PharmacyPaymentItemRequest> items
) {
}
