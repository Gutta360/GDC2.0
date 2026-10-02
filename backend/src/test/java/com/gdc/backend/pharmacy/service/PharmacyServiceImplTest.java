package com.gdc.backend.pharmacy.service;

import com.gdc.backend.patient.entity.Gender;
import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentCreateRequest;
import com.gdc.backend.pharmacy.dto.PharmacyPaymentItemRequest;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.entity.MedicineStockMovement;
import com.gdc.backend.pharmacy.entity.PaymentMode;
import com.gdc.backend.pharmacy.entity.StockMovementType;
import com.gdc.backend.pharmacy.exception.PharmacyConflictException;
import com.gdc.backend.pharmacy.exception.PharmacyValidationException;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.pharmacy.repository.MedicineStockMovementRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPaymentItemRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPaymentRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPrescriptionRepository;
import com.gdc.backend.treatment.entity.FollowUp;
import com.gdc.backend.treatment.entity.PrescriptionItem;
import com.gdc.backend.treatment.entity.Treatment;
import com.gdc.backend.treatment.entity.TreatmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PharmacyServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-02T00:00:00Z"),
            ZoneId.of("UTC")
    );

    private Patient patient;
    private Medicine medicine;
    private PrescriptionItem treatmentPrescription;
    private PrescriptionItem followUpPrescription;
    private PharmacyServiceImpl service;
    private List<MedicineStockMovement> savedMovements;

    @BeforeEach
    void setUp() throws Exception {
        patient = patient();
        medicine = medicine(1L, 5, LocalDate.parse("2027-01-08"));
        treatmentPrescription = treatmentPrescription(10L, 2);
        followUpPrescription = followUpPrescription(11L, 1);
        savedMovements = List.of();
        service = service(false, List.of(treatmentPrescription, followUpPrescription));
    }

    @Test
    void pendingPrescriptionsIncludesTreatmentAndFollowUpInDeterministicOrder() {
        var responses = service.getPendingPrescriptions("P-00001");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).sourceType()).isEqualTo("TREATMENT");
        assertThat(responses.get(1).sourceType()).isEqualTo("FOLLOW_UP");
    }

    @Test
    void successfulPaymentReducesStockAndCalculatesAuthoritativeTotal() {
        var response = service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50.25")),
                new PharmacyPaymentItemRequest(11L, new BigDecimal("10.00"))
        ));

        assertThat(response.paymentId()).isEqualTo("PAY-00001");
        assertThat(response.totalAmount()).isEqualByComparingTo("110.50");
        assertThat(medicine.getAvailableQuantity()).isEqualTo(2);
        assertThat(response.items()).hasSize(2);
        assertThat(savedMovements).hasSize(2);
        assertThat(savedMovements)
                .extracting(MedicineStockMovement::getMovementType)
                .containsOnly(StockMovementType.DISPENSE);
        assertThat(savedMovements)
                .extracting(MedicineStockMovement::getQuantityDelta)
                .containsExactly(-2, -1);
    }

    @Test
    void paymentAcceptsMaximumSupportedTotal() throws Exception {
        medicine = medicine(1L, 1, LocalDate.parse("2027-01-08"));
        treatmentPrescription = treatmentPrescription(10L, 1);
        service = service(false, List.of(treatmentPrescription));

        var response = service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("99999999.99"))
        ));

        assertThat(response.totalAmount()).isEqualByComparingTo("99999999.99");
    }

    @Test
    void paymentRejectsLineTotalOverflow() throws Exception {
        treatmentPrescription = treatmentPrescription(10L, 2);
        service = service(false, List.of(treatmentPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50000000.00"))
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("Line total exceeds");
    }

    @Test
    void paymentRejectsGrandTotalOverflow() throws Exception {
        medicine = medicine(1L, 4, LocalDate.parse("2027-01-08"));
        treatmentPrescription = treatmentPrescription(10L, 2);
        followUpPrescription = followUpPrescription(11L, 2);
        service = service(false, List.of(treatmentPrescription, followUpPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("25000000.00")),
                new PharmacyPaymentItemRequest(11L, new BigDecimal("25000000.00"))
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("Payment total exceeds");
    }

    @Test
    void duplicatePaymentAttemptIsRejected() throws Exception {
        service = service(true, List.of(treatmentPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50.00"))
        ))).isInstanceOf(PharmacyConflictException.class);
    }

    @Test
    void paymentRejectsQuantityGreaterThanStock() throws Exception {
        medicine = medicine(1L, 1, LocalDate.parse("2027-01-08"));
        treatmentPrescription = treatmentPrescription(10L, 2);
        service = service(false, List.of(treatmentPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50.00"))
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("Only 1 quantity available");
    }

    @Test
    void paymentRejectsExpiredMedicine() throws Exception {
        medicine = medicine(1L, 5, LocalDate.parse("2026-10-01"));
        treatmentPrescription = treatmentPrescription(10L, 2);
        service = service(false, List.of(treatmentPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50.00"))
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void paymentRejectsWrongPatientPrescription() throws Exception {
        Patient otherPatient = patient();
        otherPatient.setPatientId("P-00002");
        treatmentPrescription = treatmentPrescription(10L, 1, otherPatient);
        service = service(false, List.of(treatmentPrescription));

        assertThatThrownBy(() -> service.createPayment(paymentRequest(
                new PharmacyPaymentItemRequest(10L, new BigDecimal("50.00"))
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("selected patient");
    }

    private PharmacyServiceImpl service(
            boolean alreadyFulfilled,
            List<PrescriptionItem> prescriptions
    ) {
        PatientRepository patientRepository = repositoryProxy(
                PatientRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByPatientIdAndActiveTrue" -> Optional.of(patient);
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        PharmacyPrescriptionRepository prescriptionRepository = repositoryProxy(
                PharmacyPrescriptionRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findPendingForPatient", "findAllByIdForUpdate" -> prescriptions;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        MedicineRepository medicineRepository = repositoryProxy(
                MedicineRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findAllByIdForUpdate" -> List.of(medicine);
                    case "saveAllAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        PharmacyPaymentRepository paymentRepository = repositoryProxy(
                PharmacyPaymentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "getNextPaymentNumber" -> 1L;
                    case "saveAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        PharmacyPaymentItemRepository paymentItemRepository = repositoryProxy(
                PharmacyPaymentItemRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "existsByPrescriptionItemId" -> alreadyFulfilled;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        MedicineStockMovementRepository stockMovementRepository = repositoryProxy(
                MedicineStockMovementRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "saveAll" -> {
                        savedMovements = (List<MedicineStockMovement>) invocation.arguments()[0];
                        yield invocation.arguments()[0];
                    }
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        return new PharmacyServiceImpl(
                patientRepository,
                prescriptionRepository,
                medicineRepository,
                paymentRepository,
                paymentItemRepository,
                stockMovementRepository,
                CLOCK
        );
    }

    private PharmacyPaymentCreateRequest paymentRequest(PharmacyPaymentItemRequest... items) {
        return new PharmacyPaymentCreateRequest(
                "P-00001",
                PaymentMode.CASH,
                null,
                List.of(items)
        );
    }

    private PrescriptionItem treatmentPrescription(Long id, int quantity) throws Exception {
        return treatmentPrescription(id, quantity, patient);
    }

    private PrescriptionItem treatmentPrescription(Long id, int quantity, Patient itemPatient) throws Exception {
        Treatment treatment = new Treatment();
        setId(treatment, 100L + id);
        treatment.setTreatmentId("T-00001");
        treatment.setPatient(itemPatient);
        treatment.setTreatmentType(TreatmentType.ADVISED);
        treatment.setTreatmentDate(Instant.parse("2026-10-01T00:00:00Z"));
        treatment.setTreatmentAmount(BigDecimal.ZERO);
        treatment.setActive(true);

        PrescriptionItem item = new PrescriptionItem();
        setId(item, id);
        item.setTreatment(treatment);
        item.setMedicine(medicine);
        item.setQuantity(quantity);
        return item;
    }

    private PrescriptionItem followUpPrescription(Long id, int quantity) throws Exception {
        FollowUp followUp = new FollowUp();
        setId(followUp, 200L + id);
        followUp.setFollowUpId("F-00001");
        followUp.setPatient(patient);
        followUp.setFollowUpDate(Instant.parse("2026-10-02T00:00:00Z"));
        followUp.setActive(true);

        PrescriptionItem item = new PrescriptionItem();
        setId(item, id);
        item.setFollowUp(followUp);
        item.setMedicine(medicine);
        item.setQuantity(quantity);
        return item;
    }

    private Medicine medicine(Long id, int availableQuantity, LocalDate expiryDate) throws Exception {
        Medicine item = new Medicine();
        setId(item, id);
        item.setMedicineId("M-00001");
        item.setMedicineName("Chymoral forte");
        item.setAvailableQuantity(availableQuantity);
        item.setExpiryDate(expiryDate);
        item.setActive(true);
        return item;
    }

    private Patient patient() {
        Patient item = new Patient();
        item.setPatientId("P-00001");
        item.setFirstName("Anaya");
        item.setLastName("Rao");
        item.setMobile("9876543210");
        item.setGender(Gender.FEMALE);
        item.setAge((short) 30);
        item.setAddress("Clinic Road");
        item.setActive(true);
        return item;
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }

    @SuppressWarnings("unchecked")
    private <T> T repositoryProxy(
            Class<T> type,
            RepositoryInvocationHandler handler
    ) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(this, args);
                    }

                    return handler.invoke(new RepositoryInvocation(method, args == null ? new Object[0] : args));
                }
        );
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }

        if (returnType == int.class || returnType == long.class) {
            return 0;
        }

        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.empty();
        }

        if (List.class.isAssignableFrom(returnType)) {
            return List.of();
        }

        return null;
    }

    private record RepositoryInvocation(
            java.lang.reflect.Method method,
            Object[] arguments
    ) {
    }

    @FunctionalInterface
    private interface RepositoryInvocationHandler extends InvocationHandler {
        Object invoke(RepositoryInvocation invocation) throws Throwable;

        @Override
        default Object invoke(
                Object proxy,
                java.lang.reflect.Method method,
                Object[] args
        ) {
            return null;
        }
    }
}
