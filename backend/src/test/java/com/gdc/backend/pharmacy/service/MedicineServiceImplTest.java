package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.MedicineStockRequest;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.exception.PharmacyNotFoundException;
import com.gdc.backend.pharmacy.exception.PharmacyValidationException;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.pharmacy.repository.MedicineStockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicineServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-10-02T00:00:00Z"),
            ZoneId.of("UTC")
    );

    private MedicineServiceImpl service;
    private Medicine medicine;

    @BeforeEach
    void setUp() {
        medicine = medicine();
        service = service(false);
    }

    @Test
    void createMedicineCreatesStockWithGeneratedId() {
        var response = service.createMedicine(new MedicineStockRequest(
                "  KetrolDT  ",
                71,
                LocalDate.parse("2027-01-11")
        ));

        assertThat(response.medicineId()).isEqualTo("M-00004");
        assertThat(response.medicineName()).isEqualTo("KetrolDT");
        assertThat(response.availableQuantity()).isEqualTo(71);
        assertThat(response.expiryDate()).isEqualTo(LocalDate.parse("2027-01-11"));
    }

    @Test
    void createMedicineRejectsDuplicateNormalizedName() {
        service = service(true);

        assertThatThrownBy(() -> service.createMedicine(new MedicineStockRequest(
                "chymoral FORTE",
                10,
                LocalDate.parse("2027-01-08")
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createMedicineReactivatesInactiveDuplicateInsteadOfCreatingParallelRow() {
        Medicine inactive = medicine();
        inactive.setActive(false);
        inactive.setAvailableQuantity(3);
        inactive.setExpiryDate(LocalDate.parse("2026-01-08"));

        MedicineRepository repository = repositoryProxy(
                MedicineRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "existsActiveByNormalizedName" -> false;
                    case "findInactiveByNormalizedNameOrderByIdAsc" -> List.of(inactive);
                    case "saveAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        service = new MedicineServiceImpl(repository, movementRepository(), CLOCK);

        var response = service.createMedicine(new MedicineStockRequest(
                " Chymoral forte ",
                10,
                LocalDate.parse("2027-01-08")
        ));

        assertThat(response.medicineId()).isEqualTo("M-00001");
        assertThat(response.availableQuantity()).isEqualTo(10);
        assertThat(inactive.isActive()).isTrue();
    }

    @Test
    void createMedicineRejectsInvalidQuantity() {
        assertThatThrownBy(() -> service.createMedicine(new MedicineStockRequest(
                "Pan d",
                -1,
                LocalDate.parse("2027-01-08")
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void createMedicineRejectsPastExpiry() {
        assertThatThrownBy(() -> service.createMedicine(new MedicineStockRequest(
                "Pan d",
                1,
                LocalDate.parse("2026-10-01")
        ))).isInstanceOf(PharmacyValidationException.class)
                .hasMessageContaining("past");
    }

    @Test
    void updateMedicineUpdatesStock() {
        var response = service.updateMedicine("M-00001", new MedicineStockRequest(
                "Chymoral forte",
                216,
                LocalDate.parse("2027-01-08")
        ));

        assertThat(response.availableQuantity()).isEqualTo(216);
        assertThat(response.expiryDate()).isEqualTo(LocalDate.parse("2027-01-08"));
    }

    @Test
    void deactivateMedicineUsesLifecycleDelete() {
        service.deactivateMedicine("M-00001");

        assertThat(medicine.isActive()).isFalse();
    }

    @Test
    void deactivateMedicineRejectsMissingMedicine() {
        service = new MedicineServiceImpl(
                repositoryProxy(
                        MedicineRepository.class,
                        invocation -> switch (invocation.method().getName()) {
                            case "findByMedicineIdAndActiveTrue" -> Optional.empty();
                            default -> defaultValue(invocation.method().getReturnType());
                        }
                ),
                movementRepository(),
                CLOCK
        );

        assertThatThrownBy(() -> service.deactivateMedicine("M-404"))
                .isInstanceOf(PharmacyNotFoundException.class);
    }

    private MedicineServiceImpl service(boolean duplicateName) {
        MedicineRepository repository = repositoryProxy(
                MedicineRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "getNextMedicineNumber" -> 4L;
                    case "existsActiveByNormalizedName" -> duplicateName;
                    case "existsActiveByNormalizedNameAndMedicineIdNot" -> false;
                    case "findByMedicineIdAndActiveTrue" -> Optional.of(medicine);
                    case "saveAndFlush" -> invocation.arguments()[0];
                    case "findAllByActiveTrueOrderByMedicineNameAsc" -> List.of(medicine);
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        return new MedicineServiceImpl(repository, movementRepository(), CLOCK);
    }

    private MedicineStockMovementRepository movementRepository() {
        return repositoryProxy(
                MedicineStockMovementRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "save" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );
    }

    private Medicine medicine() {
        Medicine item = new Medicine();
        item.setMedicineId("M-00001");
        item.setMedicineName("Chymoral forte");
        item.setAvailableQuantity(5);
        item.setExpiryDate(LocalDate.parse("2027-01-08"));
        item.setActive(true);
        return item;
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
