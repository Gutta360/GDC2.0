package com.gdc.backend.appointment.service;

import com.gdc.backend.appointment.dto.AppointmentCreateRequest;
import com.gdc.backend.appointment.dto.AppointmentResponse;
import com.gdc.backend.appointment.dto.DoctorBusyCreateRequest;
import com.gdc.backend.appointment.entity.Appointment;
import com.gdc.backend.appointment.entity.AppointmentSlotReservation;
import com.gdc.backend.appointment.entity.AppointmentType;
import com.gdc.backend.appointment.exception.SchedulingConflictException;
import com.gdc.backend.appointment.exception.SchedulingValidationException;
import com.gdc.backend.appointment.mapper.AppointmentMapper;
import com.gdc.backend.appointment.repository.AppointmentRepository;
import com.gdc.backend.appointment.repository.AppointmentSlotReservationRepository;
import com.gdc.backend.appointment.repository.DoctorBusySlotRepository;
import com.gdc.backend.patient.entity.Gender;
import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentServiceImplTest {

    private AppointmentRepository appointmentRepository;
    private AppointmentSlotReservationRepository reservationRepository;
    private DoctorBusySlotRepository doctorBusySlotRepository;
    private PatientRepository patientRepository;
    private AppointmentServiceImpl service;

    @BeforeEach
    void setUp() {

        appointmentRepository = repositoryProxy(
                AppointmentRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "getNextAppointmentNumber" -> 1L;
                    case "saveAndFlush" -> invocation.arguments()[0];
                    case "findActiveInRangeWithPatient" -> List.of();
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        reservationRepository = repositoryProxy(
                AppointmentSlotReservationRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "saveAndFlush" -> invocation.arguments()[0];
                    case "findBySlotStartGreaterThanEqualAndSlotStartLessThanAndActiveTrue" -> List.of();
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        doctorBusySlotRepository = repositoryProxy(
                DoctorBusySlotRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "saveAndFlush" -> invocation.arguments()[0];
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        patientRepository = repositoryProxy(
                PatientRepository.class,
                invocation -> switch (invocation.method().getName()) {
                    case "findByPatientIdAndActiveTrue" -> Optional.of(patient());
                    default -> defaultValue(invocation.method().getReturnType());
                }
        );

        service = new AppointmentServiceImpl(
                appointmentRepository,
                reservationRepository,
                doctorBusySlotRepository,
                patientRepository,
                new AppointmentMapper()
        );
    }

    @Test
    void createAppointmentCalculatesBmiOnBackend() {

        AppointmentResponse response = service.createAppointment(
                baseRequest(
                        LocalTime.of(9, 30),
                        BigDecimal.valueOf(180),
                        BigDecimal.valueOf(81)
                )
        );

        assertThat(response.appointmentId()).isEqualTo("A-00001");
        assertThat(response.bmi()).isEqualByComparingTo("25.00");
    }

    @Test
    void createAppointmentRejectsNonThirtyMinuteSlot() {

        assertThatThrownBy(() -> service.createAppointment(
                baseRequest(
                        LocalTime.of(9, 15),
                        null,
                        null
                )
        ))
                .isInstanceOf(SchedulingValidationException.class)
                .hasMessageContaining("30-minute");
    }

    @Test
    void createDoctorBusyRejectsOutsideSchedulingHours() {

        DoctorBusyCreateRequest request = new DoctorBusyCreateRequest(
                LocalDate.of(2026, 10, 1),
                LocalTime.of(8, 30),
                null
        );

        assertThatThrownBy(() -> service.createDoctorBusy(request))
                .isInstanceOf(SchedulingValidationException.class)
                .hasMessageContaining("09:00");
    }

    @Test
    void createDoctorBusyMapsDatabaseUniqueViolationToConflict() {

        reservationRepository = repositoryProxy(
                AppointmentSlotReservationRepository.class,
                invocation -> {
                    if ("saveAndFlush".equals(invocation.method().getName())) {
                        throw new DataIntegrityViolationException("duplicate slot");
                    }

                    return defaultValue(invocation.method().getReturnType());
                }
        );

        service = new AppointmentServiceImpl(
                appointmentRepository,
                reservationRepository,
                doctorBusySlotRepository,
                patientRepository,
                new AppointmentMapper()
        );

        DoctorBusyCreateRequest request = new DoctorBusyCreateRequest(
                LocalDate.of(2026, 10, 1),
                LocalTime.of(10, 0),
                "Blocked"
        );

        assertThatThrownBy(() -> service.createDoctorBusy(request))
                .isInstanceOf(SchedulingConflictException.class)
                .hasMessageContaining("unavailable");
    }

    private AppointmentCreateRequest baseRequest(
            LocalTime time,
            BigDecimal heightCm,
            BigDecimal weightKg
    ) {

        return new AppointmentCreateRequest(
                "P-00001",
                LocalDate.of(2026, 10, 1),
                time,
                AppointmentType.NEW,
                null,
                null,
                null,
                null,
                null,
                heightCm,
                weightKg,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                null,
                false,
                null
        );
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

    private <T> T repositoryProxy(
            Class<T> repositoryType,
            RepositoryInvocationHandler handler
    ) {

        InvocationHandler invocationHandler = (_proxy, method, arguments) -> {

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, arguments);
            }

            return handler.invoke(new RepositoryInvocation(
                    method,
                    arguments == null ? new Object[0] : arguments
            ));
        };

        return repositoryType.cast(
                Proxy.newProxyInstance(
                        repositoryType.getClassLoader(),
                        new Class<?>[]{repositoryType},
                        invocationHandler
                )
        );
    }

    private Object defaultValue(
            Class<?> returnType
    ) {

        if (returnType == boolean.class) {
            return false;
        }

        if (returnType == long.class) {
            return 0L;
        }

        if (returnType == int.class) {
            return 0;
        }

        if (returnType == void.class) {
            return null;
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
    private interface RepositoryInvocationHandler {
        Object invoke(RepositoryInvocation invocation) throws Throwable;
    }
}
