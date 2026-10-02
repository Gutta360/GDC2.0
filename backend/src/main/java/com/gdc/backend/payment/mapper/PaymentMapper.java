package com.gdc.backend.payment.mapper;

import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.payment.dto.OutstandingTreatmentPaymentResponse;
import com.gdc.backend.payment.dto.PaymentResponse;
import com.gdc.backend.payment.entity.TreatmentPayment;
import com.gdc.backend.treatment.entity.Treatment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public OutstandingTreatmentPaymentResponse toOutstandingResponse(Treatment treatment) {
        return new OutstandingTreatmentPaymentResponse(
                treatment.getTreatmentId(),
                treatment.getTreatmentDate(),
                treatment.getTreatmentType(),
                treatment.getTreatmentAmount(),
                treatment.getTreatmentAmount()
        );
    }

    public PaymentResponse toResponse(TreatmentPayment payment) {
        Patient patient = payment.getPatient();
        Treatment treatment = payment.getTreatment();

        return new PaymentResponse(
                payment.getPaymentId(),
                patient.getPatientId(),
                fullName(patient),
                treatment.getTreatmentId(),
                treatment.getTreatmentDate(),
                treatment.getTreatmentType(),
                "Treatment",
                payment.getPaymentMode(),
                payment.getAmount(),
                payment.getDetails(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion()
        );
    }

    private String fullName(Patient patient) {
        String lastName = patient.getLastName() == null ? "" : patient.getLastName().trim();
        return (patient.getFirstName() + " " + lastName).trim();
    }
}
