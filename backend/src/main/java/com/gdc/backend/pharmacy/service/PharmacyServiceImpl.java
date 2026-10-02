package com.gdc.backend.pharmacy.service;

import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.exception.PatientNotFoundException;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.pharmacy.dto.PendingPrescriptionItemResponse;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentCreateRequest;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentItemRequest;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentItemResponse;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentResponse;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.entity.MedicineStockMovement;
import com.gdc.backend.pharmacy.entity.PaymentMode;
import com.gdc.backend.pharmacy.entity.PharmacyPayment;
import com.gdc.backend.pharmacy.entity.PharmacyPaymentItem;
import com.gdc.backend.pharmacy.entity.StockMovementType;
import com.gdc.backend.pharmacy.exception.PharmacyConflictException;
import com.gdc.backend.pharmacy.exception.PharmacyNotFoundException;
import com.gdc.backend.pharmacy.exception.PharmacyValidationException;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.pharmacy.repository.MedicineStockMovementRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPaymentItemRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPaymentRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPrescriptionRepository;
import com.gdc.backend.treatment.entity.FollowUp;
import com.gdc.backend.treatment.entity.PrescriptionItem;
import com.gdc.backend.treatment.entity.Treatment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PharmacyServiceImpl implements PharmacyService {

    private static final String PAYMENT_ID_PREFIX = "PAY-";
    private static final BigDecimal MAX_NUMERIC_10_2 = new BigDecimal("99999999.99");

    private final PatientRepository patientRepository;
    private final PharmacyPrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final PharmacyPaymentRepository paymentRepository;
    private final PharmacyPaymentItemRepository paymentItemRepository;
    private final MedicineStockMovementRepository stockMovementRepository;
    private final Clock clock;

    public PharmacyServiceImpl(
            PatientRepository patientRepository,
            PharmacyPrescriptionRepository prescriptionRepository,
            MedicineRepository medicineRepository,
            PharmacyPaymentRepository paymentRepository,
            PharmacyPaymentItemRepository paymentItemRepository,
            MedicineStockMovementRepository stockMovementRepository,
            Clock clock
    ) {
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.paymentRepository = paymentRepository;
        this.paymentItemRepository = paymentItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingPrescriptionItemResponse> getPendingPrescriptions(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        findActivePatient(normalizedPatientId);

        return prescriptionRepository
                .findPendingForPatient(normalizedPatientId)
                .stream()
                .map(this::toPendingResponse)
                .toList();
    }

    @Override
    public PharmacyPaymentResponse createPayment(PharmacyPaymentCreateRequest request) {
        String normalizedPatientId = normalizePatientId(request.patientId());
        Patient patient = findActivePatient(normalizedPatientId);
        validatePaymentDetails(request.paymentMode(), request.details());

        Map<Long, BigDecimal> pricesByPrescription = requestedPrices(request.items());
        List<Long> prescriptionIds = pricesByPrescription.keySet().stream().sorted().toList();

        List<PrescriptionItem> prescriptions = prescriptionRepository.findAllByIdForUpdate(prescriptionIds);
        if (prescriptions.size() != prescriptionIds.size()) {
            throw new PharmacyValidationException("One or more prescription items were not found");
        }

        validatePatientOwnership(normalizedPatientId, prescriptions);
        validateNotFulfilled(prescriptionIds);

        List<Long> medicineIds = prescriptions.stream()
                .map(item -> item.getMedicine().getId())
                .distinct()
                .sorted()
                .toList();
        Map<Long, Medicine> lockedMedicines = medicineRepository
                .findAllByIdForUpdate(medicineIds)
                .stream()
                .collect(Collectors.toMap(Medicine::getId, Function.identity()));

        PharmacyPayment payment = new PharmacyPayment();
        payment.setPaymentId(generatePaymentId());
        payment.setPatient(patient);
        payment.setPaymentMode(request.paymentMode());
        payment.setDetails(normalizeOptionalText(request.details()));
        payment.setPaidAt(Instant.now(clock));

        BigDecimal total = BigDecimal.ZERO;
        List<MedicineStockMovement> movements = new ArrayList<>();
        LocalDate today = LocalDate.now(clock);

        for (PrescriptionItem prescription : prescriptions.stream().sorted(Comparator.comparing(PrescriptionItem::getId)).toList()) {
            Medicine medicine = lockedMedicines.get(prescription.getMedicine().getId());
            if (medicine == null || !medicine.isActive()) {
                throw new PharmacyValidationException("Medicine is not active: " + prescription.getMedicine().getMedicineName());
            }

            if (medicine.getExpiryDate().isBefore(today)) {
                throw new PharmacyValidationException("Medicine is expired: " + medicine.getMedicineName());
            }

            int quantity = prescription.getQuantity();
            if (medicine.getAvailableQuantity() < quantity) {
                throw new PharmacyValidationException(
                        "Only " + medicine.getAvailableQuantity() + " quantity available for " + medicine.getMedicineName()
                );
            }

            BigDecimal unitPrice = pricesByPrescription.get(prescription.getId());
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
            validateCurrencyFits(lineTotal, "Line total");
            total = total.add(lineTotal);
            validateCurrencyFits(total, "Payment total");

            medicine.setAvailableQuantity(medicine.getAvailableQuantity() - quantity);

            PharmacyPaymentItem item = new PharmacyPaymentItem();
            item.setPrescriptionItem(prescription);
            item.setMedicine(medicine);
            item.setPrescribedQuantity(quantity);
            item.setDispensedQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            payment.addItem(item);

            movements.add(stockMovement(medicine, -quantity, payment.getPaymentId()));
        }

        payment.setTotalAmount(validateCurrencyFits(total.setScale(2, RoundingMode.HALF_UP), "Payment total"));

        try {
            PharmacyPayment saved = paymentRepository.saveAndFlush(payment);
            medicineRepository.saveAllAndFlush(new ArrayList<>(lockedMedicines.values()));
            stockMovementRepository.saveAll(movements);
            return toPaymentResponse(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new PharmacyConflictException("Prescription has already been fulfilled");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyPaymentResponse getPayment(String paymentId) {
        return paymentRepository
                .findByPaymentId(normalizePaymentId(paymentId))
                .map(this::toPaymentResponse)
                .orElseThrow(() -> new PharmacyNotFoundException("Payment not found: " + normalizePaymentId(paymentId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyPaymentResponse> getPayments(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        findActivePatient(normalizedPatientId);

        return paymentRepository
                .findAllByPatientPatientIdOrderByPaidAtDescIdDesc(normalizedPatientId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    private Map<Long, BigDecimal> requestedPrices(List<PharmacyPaymentItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new PharmacyValidationException("Medicine cart cannot be empty");
        }

        Map<Long, BigDecimal> prices = new LinkedHashMap<>();

        for (PharmacyPaymentItemRequest item : items) {
            if (item.prescriptionItemId() == null) {
                throw new PharmacyValidationException("Prescription item is required");
            }

            if (prices.containsKey(item.prescriptionItemId())) {
                throw new PharmacyValidationException("Duplicate prescription items are not allowed");
            }

            prices.put(item.prescriptionItemId(), validateUnitPrice(item.unitPrice()));
        }

        return prices;
    }

    private BigDecimal validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null) {
            throw new PharmacyValidationException("Unit price is required");
        }

        if (unitPrice.signum() < 0) {
            throw new PharmacyValidationException("Unit price cannot be negative");
        }

        if (unitPrice.scale() > 2 || unitPrice.precision() - unitPrice.scale() > 8) {
            throw new PharmacyValidationException("Unit price must have at most 8 integer digits and 2 decimal places");
        }

        return unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal validateCurrencyFits(BigDecimal amount, String label) {
        if (amount.compareTo(MAX_NUMERIC_10_2) > 0) {
            throw new PharmacyValidationException(label + " exceeds the maximum supported amount");
        }

        return amount;
    }

    private void validatePatientOwnership(String patientId, List<PrescriptionItem> prescriptions) {
        for (PrescriptionItem prescription : prescriptions) {
            if (!patientId.equals(patientIdFor(prescription))) {
                throw new PharmacyValidationException("Prescription item does not belong to selected patient");
            }
        }
    }

    private void validateNotFulfilled(List<Long> prescriptionIds) {
        for (Long prescriptionId : prescriptionIds) {
            if (paymentItemRepository.existsByPrescriptionItemId(prescriptionId)) {
                throw new PharmacyConflictException("Prescription has already been fulfilled");
            }
        }
    }

    private PendingPrescriptionItemResponse toPendingResponse(PrescriptionItem item) {
        Medicine medicine = item.getMedicine();
        boolean expired = medicine.getExpiryDate().isBefore(LocalDate.now(clock));

        return new PendingPrescriptionItemResponse(
                item.getId(),
                sourceTypeFor(item),
                sourceIdFor(item),
                sourceDateFor(item),
                medicine.getMedicineId(),
                medicine.getMedicineName(),
                item.getQuantity(),
                medicine.getAvailableQuantity(),
                medicine.getExpiryDate(),
                expired
        );
    }

    private PharmacyPaymentResponse toPaymentResponse(PharmacyPayment payment) {
        Patient patient = payment.getPatient();
        return new PharmacyPaymentResponse(
                payment.getPaymentId(),
                patient.getPatientId(),
                fullName(patient),
                payment.getPaymentMode(),
                payment.getTotalAmount(),
                payment.getDetails(),
                payment.getPaidAt(),
                payment.getItems().stream().map(this::toPaymentItemResponse).toList(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion()
        );
    }

    private PharmacyPaymentItemResponse toPaymentItemResponse(PharmacyPaymentItem item) {
        Medicine medicine = item.getMedicine();
        return new PharmacyPaymentItemResponse(
                item.getPrescriptionItem().getId(),
                medicine.getMedicineId(),
                medicine.getMedicineName(),
                item.getPrescribedQuantity(),
                item.getDispensedQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private MedicineStockMovement stockMovement(Medicine medicine, Integer quantityDelta, String paymentId) {
        MedicineStockMovement movement = new MedicineStockMovement();
        movement.setMedicine(medicine);
        movement.setMovementType(StockMovementType.DISPENSE);
        movement.setQuantityDelta(quantityDelta);
        movement.setReferenceType("PHARMACY_PAYMENT");
        movement.setReferenceId(paymentId);
        return movement;
    }

    private Patient findActivePatient(String patientId) {
        return patientRepository
                .findByPatientIdAndActiveTrue(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private String generatePaymentId() {
        return PAYMENT_ID_PREFIX + "%05d".formatted(paymentRepository.getNextPaymentNumber());
    }

    private String sourceTypeFor(PrescriptionItem item) {
        return item.getTreatment() != null ? "TREATMENT" : "FOLLOW_UP";
    }

    private String sourceIdFor(PrescriptionItem item) {
        Treatment treatment = item.getTreatment();
        FollowUp followUp = item.getFollowUp();
        return treatment != null ? treatment.getTreatmentId() : followUp.getFollowUpId();
    }

    private Instant sourceDateFor(PrescriptionItem item) {
        Treatment treatment = item.getTreatment();
        FollowUp followUp = item.getFollowUp();
        return treatment != null ? treatment.getTreatmentDate() : followUp.getFollowUpDate();
    }

    private String patientIdFor(PrescriptionItem item) {
        Treatment treatment = item.getTreatment();
        FollowUp followUp = item.getFollowUp();
        return treatment != null
                ? treatment.getPatient().getPatientId()
                : followUp.getPatient().getPatientId();
    }

    private String normalizePatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new PharmacyValidationException("Patient ID is required");
        }

        return patientId.trim().toUpperCase();
    }

    private String normalizePaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new PharmacyValidationException("Payment ID is required");
        }

        return paymentId.trim().toUpperCase();
    }

    private void validatePaymentDetails(PaymentMode paymentMode, String details) {
        if (paymentMode == null) {
            throw new PharmacyValidationException("Payment mode is required");
        }

        if (paymentMode == PaymentMode.UPI && normalizeOptionalText(details) == null) {
            throw new PharmacyValidationException("Payment details are required for UPI");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String fullName(Patient patient) {
        List<String> parts = List.of(
                patient.getFirstName() == null ? "" : patient.getFirstName().trim(),
                patient.getLastName() == null ? "" : patient.getLastName().trim()
        );

        String name = String.join(" ", parts).trim();
        return name.isBlank() ? patient.getPatientId() : name;
    }
}
