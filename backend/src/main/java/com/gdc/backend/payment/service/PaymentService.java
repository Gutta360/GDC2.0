package com.gdc.backend.payment.service;

import com.gdc.backend.payment.dto.OutstandingTreatmentPaymentResponse;
import com.gdc.backend.payment.dto.PaymentCreateRequest;
import com.gdc.backend.payment.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    List<OutstandingTreatmentPaymentResponse> getOutstandingTreatments(String patientId);
    PaymentResponse createPayment(PaymentCreateRequest request);
    PaymentResponse getPayment(String paymentId);
    List<PaymentResponse> getPayments(String patientId);
}
