package com.gdc.backend.payment.service;

import com.gdc.backend.patient.entity.Gender;
import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.exception.PatientNotFoundException;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.payment.dto.PaymentCreateRequest;
import com.gdc.backend.payment.entity.PaymentMode;
import com.gdc.backend.payment.entity.TreatmentPayment;
import com.gdc.backend.payment.exception.PaymentConflictException;
import com.gdc.backend.payment.exception.PaymentValidationException;
import com.gdc.backend.payment.mapper.PaymentMapper;
import com.gdc.backend.payment.repository.OutstandingTreatmentPaymentRepository;
import com.gdc.backend.payment.repository.TreatmentPaymentRepository;
import com.gdc.backend.treatment.entity.Treatment;
import com.gdc.backend.treatment.entity.TreatmentType;
import com.gdc.backend.treatment.exception.TreatmentNotFoundException;
import com.gdc.backend.treatment.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-02T00:00:00Z"),
            ZoneId.of("UTC")
    );

    private Patient patient;
    private Treatment treatment;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        patient = patient("P-00001");
        treatment = treatment("T-00001", patient, new BigDecimal("1250.50"));
        service = service(false, Optional.of(treatment), List.of(), false, Optional.of(patient));
    }

    @Test
    void createPaymentCopiesTreatmentAmountAndGeneratesTreatmentPaymentId() {
        var response = service.createPayment(request(PaymentMode.CASH, "  paid cash  "));

        assertThat(response.paymentId()).isEqualTo("TPAY-00001");
        assertThat(response.patientId()).isEqualTo("P-00001");
        assertThat(response.treatmentId()).isEqualTo("T-00001");
        assertThat(response.amount()).isEqualByComparingTo("1250.50");
        assertThat(response.paymentMode()).isEqualTo(PaymentMode.CASH);
        assertThat(response.details()).isEqualTo("paid cash");
        assertThat(response.paidAt()).isEqualTo(Instant.parse("2026-10-02T00:00:00Z"));
    }

    @Test
    void createPaymentSupportsUpiWithoutDetails() {
        var response = service.createPayment(request(PaymentMode.UPI, null));

        assertThat(response.paymentMode()).isEqualTo(PaymentMode.UPI);
        assertThat(response.details()).isNull();
    }

    @Test
    void createPaymentRejectsInactiveOrMissingPatient() {
        service = service(false, Optional.of(treatment), List.of(), false, Optional.empty());

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void createPaymentRejectsMissingTreatment() {
        service = service(false, Optional.empty(), List.of(), false, Optional.of(patient));

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(TreatmentNotFoundException.class);
    }

    @Test
    void createPaymentRejectsTreatmentBelongingToAnotherPatient() throws Exception {
        Patient otherPatient = patient("P-00002");
        treatment = treatment("T-00001", otherPatient, new BigDecimal("1250.50"));
        service = service(false, Optional.of(treatment), List.of(), false, Optional.of(patient));

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("selected patient");
    }

    @Test
    void createPaymentRejectsDuplicateTreatmentPayment() {
        service = service(true, Optional.of(treatment), List.of(), false, Optional.of(patient));

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(PaymentConflictException.class)
                .hasMessageContaining("already been paid");
    }

    @Test
    void createPaymentMapsUniqueConstraintRaceToConflict() {
        service = service(false, Optional.of(treatment), List.of(), true, Optional.of(patient));

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(PaymentConflictException.class)
                .hasMessageContaining("already been paid");
    }

    @Test
    void createPaymentRejectsMoneyOverflow() throws Exception {
        treatment = treatment("T-00001", patient, new BigDecimal("100000000.00"));
        service = service(false, Optional.of(treatment), List.of(), false, Optional.of(patient));

        assertThatThrownBy(() -> service.createPayment(request(PaymentMode.CASH, null)))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("maximum supported");
    }

    @Test
    void outstandingTreatmentsAreMappedFromRepository() {
        service = service(false, Optional.of(treatment), List.of(), false, Optional.of(patient), List.of(treatment));

        var responses = service.getOutstandingTreatments("P-00001");

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().treatmentId()).isEqualTo("T-00001");
        assertThat(responses.getFirst().outstandingAmount()).isEqualByComparingTo("1250.50");
    }

    @Test
    void historyIsFilteredByPatientAndUsesRepositoryOrder() throws Exception {
        TreatmentPayment newest = payment("TPAY-00002", patient, treatment, Instant.parse("2026-10-03T00:00:00Z"));
        TreatmentPayment oldest = payment("TPAY-00001", patient, treatment, Instant.parse("2026-10-02T00:00:00Z"));
        service = service(false, Optional.of(treatment), List.of(newest, oldest), false, Optional.of(patient));

        var responses = service.getPayments("P-00001");

        assertThat(responses).extracting(response -> response.paymentId())
                .containsExactly("TPAY-00002", "TPAY-00001");
    }

    @Test
    void emptyHistoryIsSafe() {
        var responses = service.getPayments("P-00001");

        assertThat(responses).isEmpty();
    }

    private PaymentServiceImpl service(
            boolean alreadyPaid,
            Optional<Treatment> lockedTreatment,
            List<TreatmentPayment> history,
            boolean failSave,
            Optional<Patient> activePatient
    ) {
        return service(alreadyPaid, lockedTreatment, history, failSave, activePatient, List.of());
    }

    private PaymentServiceImpl service(
            boolean alreadyPaid,
            Optional<Treatment> lockedTreatment,
            List<TreatmentPayment> history,
            boolean failSave,
            Optional<Patient> activePatient,
            List<Treatment> outstandingTreatments
    ) {
        PatientRepository patientRepository = repositoryProxy(
                PatientRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByPatientIdAndActiveTrue" -> activePatient;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        TreatmentRepository treatmentRepository = repositoryProxy(
                TreatmentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByTreatmentIdAndActiveTrueForUpdate" -> lockedTreatment;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        TreatmentPaymentRepository paymentRepository = repositoryProxy(
                TreatmentPaymentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "existsByTreatmentTreatmentId" -> alreadyPaid;
                    case "getNextTreatmentPaymentNumber" -> 1L;
                    case "saveAndFlush" -> {
                        if (failSave) {
                            throw new DataIntegrityViolationException("duplicate");
                        }
                        yield invocation.arguments()[0];
                    }
                    case "findAllByPatientPatientIdOrderByPaidAtDescIdDesc" -> history;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        OutstandingTreatmentPaymentRepository outstandingRepository = repositoryProxy(
                OutstandingTreatmentPaymentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findOutstandingTreatmentsForPatient" -> outstandingTreatments;
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        return new PaymentServiceImpl(
                patientRepository,
                treatmentRepository,
                paymentRepository,
                outstandingRepository,
                new PaymentMapper(),
                CLOCK
        );
    }

    private PaymentCreateRequest request(PaymentMode paymentMode, String details) {
        return new PaymentCreateRequest("P-00001", "T-00001", paymentMode, details);
    }

    private Patient patient(String patientId) {
        Patient item = new Patient();
        item.setPatientId(patientId);
        item.setFirstName("Anaya");
        item.setLastName("Rao");
        item.setMobile("9876543210");
        item.setGender(Gender.FEMALE);
        item.setAge((short) 30);
        item.setAddress("Clinic Road");
        item.setActive(true);
        return item;
    }

    private Treatment treatment(String treatmentId, Patient treatmentPatient, BigDecimal amount) throws Exception {
        Treatment item = new Treatment();
        setId(item, 10L);
        item.setTreatmentId(treatmentId);
        item.setPatient(treatmentPatient);
        item.setTreatmentDate(Instant.parse("2026-10-01T00:00:00Z"));
        item.setTreatmentType(TreatmentType.ADVISED);
        item.setTreatmentAmount(amount);
        item.setActive(true);
        return item;
    }

    private TreatmentPayment payment(
            String paymentId,
            Patient paymentPatient,
            Treatment paymentTreatment,
            Instant paidAt
    ) {
        TreatmentPayment payment = new TreatmentPayment();
        payment.setPaymentId(paymentId);
        payment.setPatient(paymentPatient);
        payment.setTreatment(paymentTreatment);
        payment.setPaymentMode(PaymentMode.CASH);
        payment.setAmount(paymentTreatment.getTreatmentAmount());
        payment.setPaidAt(paidAt);
        return payment;
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
