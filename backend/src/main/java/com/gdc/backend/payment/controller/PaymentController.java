package com.gdc.backend.payment.controller;

import com.gdc.backend.payment.dto.OutstandingTreatmentPaymentResponse;
import com.gdc.backend.payment.dto.PaymentCreateRequest;
import com.gdc.backend.payment.dto.PaymentResponse;
import com.gdc.backend.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/outstanding")
    public ResponseEntity<List<OutstandingTreatmentPaymentResponse>> getOutstandingTreatments(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(paymentService.getOutstandingTreatments(patientId));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity
                .created(URI.create("/api/v1/payments/" + response.paymentId()))
                .body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(paymentService.getPayments(patientId));
    }
}
