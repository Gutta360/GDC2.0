package com.gdc.backend.payment.dto;

import com.gdc.backend.payment.entity.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentCreateRequest(
        @NotBlank(message = "Patient ID is required")
        String patientId,

        @NotBlank(message = "Treatment ID is required")
        String treatmentId,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode,

        @Size(max = 1000, message = "Payment details must be at most 1000 characters")
        String details
) {
}
