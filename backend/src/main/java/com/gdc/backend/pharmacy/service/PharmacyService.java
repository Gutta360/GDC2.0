package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.PendingPrescriptionItemResponse;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentCreateRequest;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentResponse;

import java.util.List;

public interface PharmacyService {

    List<PendingPrescriptionItemResponse> getPendingPrescriptions(String patientId);

    PharmacyPaymentResponse createPayment(PharmacyPaymentCreateRequest request);

    PharmacyPaymentResponse getPayment(String paymentId);

    List<PharmacyPaymentResponse> getPayments(String patientId);
}
