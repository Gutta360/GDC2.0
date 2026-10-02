package com.gdc.backend.payment.service;

import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.exception.PatientNotFoundException;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.payment.dto.OutstandingTreatmentPaymentResponse;
import com.gdc.backend.payment.dto.PaymentCreateRequest;
import com.gdc.backend.payment.dto.PaymentResponse;
import com.gdc.backend.payment.entity.TreatmentPayment;
import com.gdc.backend.payment.exception.PaymentConflictException;
import com.gdc.backend.payment.exception.PaymentNotFoundException;
import com.gdc.backend.payment.exception.PaymentValidationException;
import com.gdc.backend.payment.mapper.PaymentMapper;
import com.gdc.backend.payment.repository.OutstandingTreatmentPaymentRepository;
import com.gdc.backend.payment.repository.TreatmentPaymentRepository;
import com.gdc.backend.treatment.entity.Treatment;
import com.gdc.backend.treatment.exception.TreatmentNotFoundException;
import com.gdc.backend.treatment.repository.TreatmentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_ID_PREFIX = "TPAY-";
    private static final BigDecimal MAX_NUMERIC_10_2 = new BigDecimal("99999999.99");

    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentPaymentRepository paymentRepository;
    private final OutstandingTreatmentPaymentRepository outstandingRepository;
    private final PaymentMapper paymentMapper;
    private final Clock clock;

    public PaymentServiceImpl(
            PatientRepository patientRepository,
            TreatmentRepository treatmentRepository,
            TreatmentPaymentRepository paymentRepository,
            OutstandingTreatmentPaymentRepository outstandingRepository,
            PaymentMapper paymentMapper,
            Clock clock
    ) {
        this.patientRepository = patientRepository;
        this.treatmentRepository = treatmentRepository;
        this.paymentRepository = paymentRepository;
        this.outstandingRepository = outstandingRepository;
        this.paymentMapper = paymentMapper;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutstandingTreatmentPaymentResponse> getOutstandingTreatments(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        findActivePatient(normalizedPatientId);

        return outstandingRepository
                .findOutstandingTreatmentsForPatient(normalizedPatientId)
                .stream()
                .map(paymentMapper::toOutstandingResponse)
                .toList();
    }

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        String normalizedPatientId = normalizePatientId(request.patientId());
        String normalizedTreatmentId = normalizeTreatmentId(request.treatmentId());
        Patient patient = findActivePatient(normalizedPatientId);

        Treatment treatment = treatmentRepository
                .findByTreatmentIdAndActiveTrueForUpdate(normalizedTreatmentId)
                .orElseThrow(() -> new TreatmentNotFoundException(normalizedTreatmentId));

        if (!normalizedPatientId.equals(treatment.getPatient().getPatientId())) {
            throw new PaymentValidationException("Treatment does not belong to selected patient");
        }

        if (paymentRepository.existsByTreatmentTreatmentId(normalizedTreatmentId)) {
            throw new PaymentConflictException("This treatment has already been paid. Refresh and try again.");
        }

        BigDecimal amount = treatment.getTreatmentAmount().setScale(2, RoundingMode.HALF_UP);
        validateAmount(amount);

        TreatmentPayment payment = new TreatmentPayment();
        payment.setPaymentId(generatePaymentId());
        payment.setPatient(patient);
        payment.setTreatment(treatment);
        payment.setPaymentMode(request.paymentMode());
        payment.setAmount(amount);
        payment.setDetails(normalizeOptionalText(request.details()));
        payment.setPaidAt(Instant.now(clock));

        try {
            return paymentMapper.toResponse(paymentRepository.saveAndFlush(payment));
        } catch (DataIntegrityViolationException exception) {
            throw new PaymentConflictException("This treatment has already been paid. Refresh and try again.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        return paymentRepository
                .findByPaymentId(normalizePaymentId(paymentId))
                .map(paymentMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + normalizePaymentId(paymentId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        findActivePatient(normalizedPatientId);

        return paymentRepository
                .findAllByPatientPatientIdOrderByPaidAtDescIdDesc(normalizedPatientId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private Patient findActivePatient(String patientId) {
        return patientRepository
                .findByPatientIdAndActiveTrue(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private String generatePaymentId() {
        return PAYMENT_ID_PREFIX + "%05d".formatted(paymentRepository.getNextTreatmentPaymentNumber());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new PaymentValidationException("Treatment amount cannot be negative");
        }

        if (amount.compareTo(MAX_NUMERIC_10_2) > 0) {
            throw new PaymentValidationException("Treatment amount exceeds the maximum supported amount");
        }
    }

    private String normalizePatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new PaymentValidationException("Patient ID is required");
        }

        return patientId.trim().toUpperCase();
    }

    private String normalizeTreatmentId(String treatmentId) {
        if (treatmentId == null || treatmentId.isBlank()) {
            throw new PaymentValidationException("Treatment ID is required");
        }

        return treatmentId.trim().toUpperCase();
    }

    private String normalizePaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new PaymentValidationException("Payment ID is required");
        }

        return paymentId.trim().toUpperCase();
    }

    private String normalizeOptionalText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String normalized = text.trim();
        if (normalized.length() > 1000) {
            throw new PaymentValidationException("Payment details must be at most 1000 characters");
        }

        return normalized;
    }
}
