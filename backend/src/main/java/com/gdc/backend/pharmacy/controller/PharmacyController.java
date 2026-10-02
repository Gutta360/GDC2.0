package com.gdc.backend.pharmacy.controller;

import com.gdc.backend.pharmacy.dto.PendingPrescriptionItemResponse;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentCreateRequest;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentResponse;
import com.gdc.backend.pharmacy.service.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/pending-prescriptions")
    public ResponseEntity<List<PendingPrescriptionItemResponse>> getPendingPrescriptions(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(pharmacyService.getPendingPrescriptions(patientId));
    }

    @PostMapping("/payments")
    public ResponseEntity<PharmacyPaymentResponse> createPayment(
            @Valid @RequestBody PharmacyPaymentCreateRequest request
    ) {
        PharmacyPaymentResponse response = pharmacyService.createPayment(request);
        return ResponseEntity
                .created(URI.create("/api/v1/pharmacy/payments/" + response.paymentId()))
                .body(response);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PharmacyPaymentResponse> getPayment(
            @PathVariable String paymentId
    ) {
        return ResponseEntity.ok(pharmacyService.getPayment(paymentId));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PharmacyPaymentResponse>> getPayments(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(pharmacyService.getPayments(patientId));
    }
}
