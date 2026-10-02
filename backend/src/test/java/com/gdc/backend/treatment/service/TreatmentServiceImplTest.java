package com.gdc.backend.treatment.service;

import com.gdc.backend.patient.entity.Gender;
import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.treatment.dto.PrescriptionItemRequest;
import com.gdc.backend.treatment.dto.ProblemRequest;
import com.gdc.backend.treatment.dto.FollowUpCreateRequest;
import com.gdc.backend.treatment.dto.TreatmentCreateRequest;
import com.gdc.backend.treatment.dto.TreatmentResponse;
import com.gdc.backend.treatment.entity.ProblemType;
import com.gdc.backend.treatment.entity.TreatmentType;
import com.gdc.backend.treatment.exception.TreatmentValidationException;
import com.gdc.backend.treatment.mapper.TreatmentMapper;
import com.gdc.backend.treatment.repository.FollowUpRepository;
import com.gdc.backend.treatment.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreatmentServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-02T00:00:00Z"),
            ZoneId.of("UTC")
    );

    @TempDir
    Path scanDir;

    private TreatmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = serviceWithRepositories(
                defaultTreatmentRepository(),
                defaultFollowUpRepository()
        );
    }

    @Test
    void createTreatmentPersistsStructuredProblemAndMedicine() {

        TreatmentResponse response = service.createTreatment(
                baseRequest(
                        List.of(new ProblemRequest(
                                ProblemType.FILLING,
                                List.of(11, 12),
                                "Composite",
                                null,
                                null,
                                List.of()
                        )),
                        List.of(new PrescriptionItemRequest("M-00001", 2))
                ),
                List.of()
        );

        assertThat(response.treatmentId()).isEqualTo("T-00001");
        assertThat(response.problems()).hasSize(1);
        assertThat(response.problems().getFirst().teeth()).containsExactly(11, 12);
        assertThat(response.prescribedMedicines()).hasSize(1);
        assertThat(response.prescribedMedicines().getFirst().quantity()).isEqualTo(2);
    }

    @Test
    void createTreatmentRejectsInvalidToothNumber() {

        TreatmentCreateRequest request = baseRequest(
                List.of(new ProblemRequest(
                        ProblemType.FILLING,
                        List.of(99),
                        null,
                        null,
                        null,
                        List.of()
                )),
                List.of()
        );

        assertThatThrownBy(() -> service.createTreatment(request, List.of()))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Invalid FDI tooth");
    }

    @Test
    void createTreatmentRejectsMissingMedicineQuantity() {

        TreatmentCreateRequest request = baseRequest(
                List.of(),
                List.of(new PrescriptionItemRequest("M-00001", null))
        );

        assertThatThrownBy(() -> service.createTreatment(request, List.of()))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Medicine quantity is required");
    }

    @Test
    void createTreatmentRejectsNonPositiveMedicineQuantity() {

        TreatmentCreateRequest request = baseRequest(
                List.of(),
                List.of(new PrescriptionItemRequest("M-00001", 0))
        );

        assertThatThrownBy(() -> service.createTreatment(request, List.of()))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Medicine quantity must be at least 1");
    }

    @Test
    void createTreatmentRejectsMedicineQuantityAboveAvailability() {

        TreatmentCreateRequest request = baseRequest(
                List.of(),
                List.of(new PrescriptionItemRequest("M-00001", 7))
        );

        assertThatThrownBy(() -> service.createTreatment(request, List.of()))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Only 5 quantity available");
    }

    @Test
    void createTreatmentRejectsExpiredMedicine() {

        service = serviceWithRepositories(
                defaultTreatmentRepository(),
                defaultFollowUpRepository(),
                LocalDate.parse("2026-10-01")
        );

        TreatmentCreateRequest request = baseRequest(
                List.of(),
                List.of(new PrescriptionItemRequest("M-00001", 1))
        );

        assertThatThrownBy(() -> service.createTreatment(request, List.of()))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Medicine is expired");
    }

    @Test
    void createTreatmentRejectsEmptyScan() {

        MultipartFile emptyScan = new MockMultipartFile(
                "scans",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> service.createTreatment(baseRequest(List.of(), List.of()), List.of(emptyScan)))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("Scan file cannot be empty");
    }

    @Test
    void failedTreatmentSaveDeletesNewlyStoredScanFiles() throws IOException {

        service = serviceWithRepositories(
                repositoryProxy(
                        TreatmentRepository.class,
                        invocation -> switch (invocation.method().getName()) {
                            case "getNextTreatmentNumber" -> 1L;
                            case "saveAndFlush" -> throw new IllegalStateException("DB failure");
                            default -> defaultValue(invocation.method().getReturnType());
                        }
                ),
                defaultFollowUpRepository()
        );

        assertThatThrownBy(() -> service.createTreatment(
                baseRequest(List.of(), List.of()),
                List.of(validJpeg())
        )).isInstanceOf(IllegalStateException.class);

        assertThat(storedScanCount()).isZero();
    }

    @Test
    void failedFollowUpSaveDeletesNewlyStoredScanFiles() throws IOException {

        service = serviceWithRepositories(
                defaultTreatmentRepository(),
                repositoryProxy(
                        FollowUpRepository.class,
                        invocation -> switch (invocation.method().getName()) {
                            case "getNextFollowUpNumber" -> 1L;
                            case "saveAndFlush" -> throw new IllegalStateException("DB failure");
                            default -> defaultValue(invocation.method().getReturnType());
                        }
                )
        );

        assertThatThrownBy(() -> service.createFollowUp(
                baseFollowUpRequest(),
                List.of(validJpeg())
        )).isInstanceOf(IllegalStateException.class);

        assertThat(storedScanCount()).isZero();
    }

    @Test
    void successfulTreatmentSaveRetainsScanFiles() throws IOException {

        service.createTreatment(
                baseRequest(List.of(), List.of()),
                List.of(validJpeg())
        );

        assertThat(storedScanCount()).isEqualTo(1);
    }

    private TreatmentServiceImpl serviceWithRepositories(
            TreatmentRepository treatmentRepository,
            FollowUpRepository followUpRepository
    ) {
        return serviceWithRepositories(
                treatmentRepository,
                followUpRepository,
                LocalDate.parse("2027-01-08")
        );
    }

    private TreatmentServiceImpl serviceWithRepositories(
            TreatmentRepository treatmentRepository,
            FollowUpRepository followUpRepository,
            LocalDate medicineExpiryDate
    ) {

        PatientRepository patientRepository = repositoryProxy(
                PatientRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByPatientIdAndActiveTrue" -> Optional.of(patient());
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        MedicineRepository medicineRepository = repositoryProxy(
                MedicineRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByMedicineIdAndActiveTrue" -> Optional.of(medicine(medicineExpiryDate));
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        return new TreatmentServiceImpl(
                patientRepository,
                medicineRepository,
                treatmentRepository,
                followUpRepository,
                new TreatmentMapper(),
                new ScanStorageService(scanDir.toString()),
                CLOCK
        );
    }

    private TreatmentRepository defaultTreatmentRepository() {
        return repositoryProxy(
                TreatmentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "getNextTreatmentNumber" -> 1L;
                    case "saveAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );
    }

    private FollowUpRepository defaultFollowUpRepository() {
        return repositoryProxy(
                FollowUpRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "getNextFollowUpNumber" -> 1L;
                    case "saveAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );
    }

    private TreatmentCreateRequest baseRequest(
            List<ProblemRequest> problems,
            List<PrescriptionItemRequest> medicines
    ) {

        return new TreatmentCreateRequest(
                "P-00001",
                Instant.parse("2026-10-02T00:00:00Z"),
                TreatmentType.ADVISED,
                BigDecimal.valueOf(1000),
                "Notes",
                problems,
                medicines
        );
    }

    private FollowUpCreateRequest baseFollowUpRequest() {

        return new FollowUpCreateRequest(
                "P-00001",
                null,
                Instant.parse("2026-10-02T00:00:00Z"),
                "Notes",
                List.of(),
                List.of()
        );
    }

    private MockMultipartFile validJpeg() {
        return new MockMultipartFile(
                "scans",
                "scan.jpg",
                "image/jpeg",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );
    }

    private long storedScanCount() throws IOException {
        if (!Files.exists(scanDir)) {
            return 0;
        }

        try (var stream = Files.list(scanDir)) {
            return stream.count();
        }
    }

    private Patient patient() {

        Patient patient = new Patient();
        patient.setPatientId("P-00001");
        patient.setFirstName("Anaya");
        patient.setLastName("Rao");
        patient.setMobile("9876543210");
        patient.setGender(Gender.FEMALE);
        patient.setAge((short) 30);
        patient.setAddress("Clinic Road");
        patient.setActive(true);

        return patient;
    }

    private Medicine medicine(LocalDate expiryDate) {

        Medicine medicine = new Medicine();
        medicine.setMedicineId("M-00001");
        medicine.setMedicineName("Chymoral forte");
        medicine.setAvailableQuantity(5);
        medicine.setExpiryDate(expiryDate);
        medicine.setActive(true);

        return medicine;
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

                    return handler.invoke(
                            new RepositoryInvocation(
                                    method,
                                    args == null ? new Object[0] : args
                            )
                    );
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
