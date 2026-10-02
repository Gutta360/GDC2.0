package com.gdc.backend.appointment.service;

import com.gdc.backend.appointment.dto.AppointmentAvailabilityResponse;
import com.gdc.backend.appointment.dto.AppointmentCalendarDayResponse;
import com.gdc.backend.appointment.dto.AppointmentCreateRequest;
import com.gdc.backend.appointment.dto.AppointmentDayResponse;
import com.gdc.backend.appointment.dto.AppointmentResponse;
import com.gdc.backend.appointment.dto.AppointmentSlotResponse;
import com.gdc.backend.appointment.dto.DoctorBusyCreateRequest;
import com.gdc.backend.appointment.dto.DoctorBusySlotResponse;
import com.gdc.backend.appointment.entity.Appointment;
import com.gdc.backend.appointment.entity.AppointmentSlotReservation;
import com.gdc.backend.appointment.entity.AppointmentType;
import com.gdc.backend.appointment.entity.DoctorBusySlot;
import com.gdc.backend.appointment.entity.ReservationType;
import com.gdc.backend.appointment.exception.AppointmentNotFoundException;
import com.gdc.backend.appointment.exception.DoctorBusyNotFoundException;
import com.gdc.backend.appointment.exception.SchedulingConflictException;
import com.gdc.backend.appointment.exception.SchedulingValidationException;
import com.gdc.backend.appointment.mapper.AppointmentMapper;
import com.gdc.backend.appointment.repository.AppointmentRepository;
import com.gdc.backend.appointment.repository.AppointmentSlotReservationRepository;
import com.gdc.backend.appointment.repository.DoctorBusySlotRepository;
import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.exception.PatientNotFoundException;
import com.gdc.backend.patient.repository.PatientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final String APPOINTMENT_ID_PREFIX = "A-";
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");
    private static final LocalTime FIRST_SLOT = LocalTime.of(9, 0);
    private static final LocalTime LAST_SLOT = LocalTime.of(21, 30);
    private static final int SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotReservationRepository reservationRepository;
    private final DoctorBusySlotRepository doctorBusySlotRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            AppointmentSlotReservationRepository reservationRepository,
            DoctorBusySlotRepository doctorBusySlotRepository,
            PatientRepository patientRepository,
            AppointmentMapper appointmentMapper
    ) {
        this.appointmentRepository = appointmentRepository;
        this.reservationRepository = reservationRepository;
        this.doctorBusySlotRepository = doctorBusySlotRepository;
        this.patientRepository = patientRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {

        Patient patient = patientRepository
                .findByPatientIdAndActiveTrue(normalizePatientId(request.patientId()))
                .orElseThrow(() -> new PatientNotFoundException(request.patientId()));

        Instant appointmentInstant = toSlotInstant(
                request.appointmentDate(),
                request.appointmentTime()
        );

        AppointmentSlotReservation reservation = reserveSlot(
                appointmentInstant,
                ReservationType.APPOINTMENT
        );

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(generateAppointmentId());
        appointment.setSlotReservation(reservation);
        appointment.setPatient(patient);
        appointment.setAppointmentDateTime(appointmentInstant);
        appointment.setAppointmentType(request.appointmentType());
        appointment.setNotes(normalizeOptionalText(request.notes()));
        appointment.setSystolicBp(request.systolicBp());
        appointment.setDiastolicBp(request.diastolicBp());
        appointment.setHeartRate(request.heartRate());
        appointment.setBreathingRate(request.breathingRate());
        appointment.setHeightCm(request.heightCm());
        appointment.setWeightKg(request.weightKg());
        appointment.setBmi(calculateBmi(request.heightCm(), request.weightKg()));
        appointment.setFbs(request.fbs());
        appointment.setRbs(request.rbs());
        appointment.setHasDiabetes(Boolean.TRUE.equals(request.hasDiabetes()));
        appointment.setHasHypertension(Boolean.TRUE.equals(request.hasHypertension()));
        appointment.setHasHeartDisease(Boolean.TRUE.equals(request.hasHeartDisease()));
        appointment.setHasAsthma(Boolean.TRUE.equals(request.hasAsthma()));
        appointment.setHasKidneyDisease(Boolean.TRUE.equals(request.hasKidneyDisease()));
        appointment.setHasLiverDisease(Boolean.TRUE.equals(request.hasLiverDisease()));
        appointment.setHasThyroidDisorder(Boolean.TRUE.equals(request.hasThyroidDisorder()));
        appointment.setHasBleedingDisorders(Boolean.TRUE.equals(request.hasBleedingDisorders()));
        appointment.setHasNeurologicalIssues(Boolean.TRUE.equals(request.hasNeurologicalIssues()));
        appointment.setHasDrugAllergy(Boolean.TRUE.equals(request.hasDrugAllergy()));
        appointment.setHasFoodAllergy(Boolean.TRUE.equals(request.hasFoodAllergy()));
        appointment.setHasLatexAllergy(Boolean.TRUE.equals(request.hasLatexAllergy()));
        appointment.setOtherAllergyNotes(normalizeOptionalText(request.otherAllergyNotes()));
        appointment.setPastSurgicalHistory(normalizeOptionalText(request.pastSurgicalHistory()));
        appointment.setHasRootCanal(Boolean.TRUE.equals(request.hasRootCanal()));
        appointment.setHasImplants(Boolean.TRUE.equals(request.hasImplants()));
        appointment.setHasCrownsOrBridges(Boolean.TRUE.equals(request.hasCrownsOrBridges()));
        appointment.setHasBraces(Boolean.TRUE.equals(request.hasBraces()));
        appointment.setHasDentures(Boolean.TRUE.equals(request.hasDentures()));
        appointment.setDentalComplicationNotes(normalizeOptionalText(request.dentalComplicationNotes()));
        appointment.setConsentGiven(Boolean.TRUE.equals(request.consentGiven()));
        appointment.setConsentReference(normalizeOptionalText(request.consentReference()));

        Appointment savedAppointment = appointmentRepository.saveAndFlush(appointment);
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(String appointmentId) {

        Appointment appointment = appointmentRepository
                .findByAppointmentIdAndActiveTrue(normalizeAppointmentId(appointmentId))
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getLatestForPatient(String patientId) {

        String normalizedPatientId = normalizePatientId(patientId);

        Appointment appointment = appointmentRepository
                .findFirstByPatientPatientIdAndActiveTrueOrderByAppointmentDateTimeDescIdDesc(
                        normalizedPatientId
                )
                .orElseThrow(
                        () -> new AppointmentNotFoundException(
                                normalizedPatientId
                        )
                );

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentCalendarDayResponse> getCalendar(
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(from, to);

        Instant fromInstant = startOfDay(from);
        Instant toInstant = startOfDay(to.plusDays(1));

        Map<LocalDate, CalendarCounts> countsByDate = new LinkedHashMap<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            countsByDate.put(date, new CalendarCounts());
        }

        for (Appointment appointment : appointmentRepository.findActiveInRangeWithPatient(fromInstant, toInstant)) {
            LocalDate date = appointment.getAppointmentDateTime().atZone(CLINIC_ZONE).toLocalDate();
            CalendarCounts counts = countsByDate.get(date);

            if (counts == null) {
                continue;
            }

            if (appointment.getAppointmentType() == AppointmentType.FOLLOW_UP) {
                counts.followUps++;
            } else {
                counts.newAppointments++;
            }
        }

        for (DoctorBusySlot busySlot : doctorBusySlotRepository
                .findByBusyDateTimeGreaterThanEqualAndBusyDateTimeLessThanAndActiveTrueOrderByBusyDateTimeAsc(
                        fromInstant,
                        toInstant
                )) {
            LocalDate date = busySlot.getBusyDateTime().atZone(CLINIC_ZONE).toLocalDate();
            CalendarCounts counts = countsByDate.get(date);

            if (counts != null) {
                counts.busySlots++;
            }
        }

        return countsByDate.entrySet()
                .stream()
                .map(entry -> new AppointmentCalendarDayResponse(
                        entry.getKey(),
                        entry.getValue().newAppointments,
                        entry.getValue().followUps,
                        entry.getValue().busySlots
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDayResponse getDay(LocalDate date) {

        Instant from = startOfDay(date);
        Instant to = startOfDay(date.plusDays(1));

        List<Appointment> appointments =
                appointmentRepository.findActiveInRangeWithPatient(from, to);

        List<DoctorBusySlot> busySlots =
                doctorBusySlotRepository
                        .findByBusyDateTimeGreaterThanEqualAndBusyDateTimeLessThanAndActiveTrueOrderByBusyDateTimeAsc(
                                from,
                                to
                        );

        return new AppointmentDayResponse(
                date,
                appointments.stream()
                        .map(appointmentMapper::toDayItem)
                        .toList(),
                busySlots.stream()
                        .map(appointmentMapper::toBusyResponse)
                        .toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentAvailabilityResponse getAvailability(LocalDate date) {

        Instant from = startOfDay(date);
        Instant to = startOfDay(date.plusDays(1));

        Map<LocalTime, String> occupiedSlots =
                reservationRepository
                        .findBySlotStartGreaterThanEqualAndSlotStartLessThanAndActiveTrue(from, to)
                        .stream()
                        .collect(Collectors.toMap(
                                reservation -> reservation.getSlotStart().atZone(CLINIC_ZONE).toLocalTime(),
                                reservation -> reservation.getReservationType().name(),
                                (first, second) -> first
                        ));

        List<AppointmentSlotResponse> slots = new ArrayList<>();

        for (LocalTime time = FIRST_SLOT; !time.isAfter(LAST_SLOT); time = time.plusMinutes(SLOT_MINUTES)) {
            String occupiedBy = occupiedSlots.get(time);
            slots.add(new AppointmentSlotResponse(
                    time,
                    occupiedBy == null,
                    occupiedBy
            ));
        }

        return new AppointmentAvailabilityResponse(date, slots);
    }

    @Override
    public DoctorBusySlotResponse createDoctorBusy(DoctorBusyCreateRequest request) {

        Instant busyInstant = toSlotInstant(
                request.date(),
                request.time()
        );

        AppointmentSlotReservation reservation = reserveSlot(
                busyInstant,
                ReservationType.DOCTOR_BUSY
        );

        DoctorBusySlot busySlot = new DoctorBusySlot();
        busySlot.setSlotReservation(reservation);
        busySlot.setBusyDateTime(busyInstant);
        busySlot.setNotes(normalizeOptionalText(request.notes()));

        DoctorBusySlot savedBusySlot =
                doctorBusySlotRepository.saveAndFlush(busySlot);

        return appointmentMapper.toBusyResponse(savedBusySlot);
    }

    @Override
    public void deleteDoctorBusy(Long id) {

        DoctorBusySlot busySlot = doctorBusySlotRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new DoctorBusyNotFoundException(id));

        busySlot.setActive(false);
        busySlot.setDeletedAt(Instant.now());
        busySlot.getSlotReservation().setActive(false);
        busySlot.getSlotReservation().setReleasedAt(Instant.now());

        doctorBusySlotRepository.save(busySlot);
    }

    private AppointmentSlotReservation reserveSlot(
            Instant slotStart,
            ReservationType reservationType
    ) {

        AppointmentSlotReservation reservation = new AppointmentSlotReservation();
        reservation.setSlotStart(slotStart);
        reservation.setReservationType(reservationType);

        try {
            return reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException exception) {
            throw new SchedulingConflictException("Selected time slot is already unavailable");
        }
    }

    private Instant toSlotInstant(
            LocalDate date,
            LocalTime time
    ) {

        if (date == null || time == null) {
            throw new SchedulingValidationException("Appointment date and time are required");
        }

        if (time.getSecond() != 0 || time.getNano() != 0 || time.getMinute() % SLOT_MINUTES != 0) {
            throw new SchedulingValidationException("Time must be on a 30-minute slot boundary");
        }

        if (time.isBefore(FIRST_SLOT) || time.isAfter(LAST_SLOT)) {
            throw new SchedulingValidationException("Time must be between 09:00 and 21:30");
        }

        return date
                .atTime(time)
                .atZone(CLINIC_ZONE)
                .toInstant();
    }

    private Instant startOfDay(LocalDate date) {

        if (date == null) {
            throw new SchedulingValidationException("Date is required");
        }

        return date.atStartOfDay(CLINIC_ZONE).toInstant();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {

        if (from == null || to == null) {
            throw new SchedulingValidationException("From and to dates are required");
        }

        if (to.isBefore(from)) {
            throw new SchedulingValidationException("To date must be on or after from date");
        }

        if (from.plusDays(62).isBefore(to)) {
            throw new SchedulingValidationException("Date range must not exceed 63 days");
        }
    }

    private String generateAppointmentId() {
        Long nextNumber = appointmentRepository.getNextAppointmentNumber();
        return APPOINTMENT_ID_PREFIX + "%05d".formatted(nextNumber);
    }

    private String normalizePatientId(String patientId) {

        if (patientId == null || patientId.isBlank()) {
            throw new SchedulingValidationException("Patient ID is required");
        }

        return patientId.trim().toUpperCase();
    }

    private String normalizeAppointmentId(String appointmentId) {

        if (appointmentId == null || appointmentId.isBlank()) {
            throw new SchedulingValidationException("Appointment ID is required");
        }

        return appointmentId.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal calculateBmi(
            BigDecimal heightCm,
            BigDecimal weightKg
    ) {

        if (heightCm == null || weightKg == null || BigDecimal.ZERO.compareTo(heightCm) == 0) {
            return null;
        }

        BigDecimal heightMeters = heightCm.divide(
                BigDecimal.valueOf(100),
                6,
                RoundingMode.HALF_UP
        );

        return weightKg.divide(
                heightMeters.multiply(heightMeters),
                2,
                RoundingMode.HALF_UP
        );
    }

    private static final class CalendarCounts {
        long newAppointments;
        long followUps;
        long busySlots;
    }
}
