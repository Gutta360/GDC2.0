package com.gdc.backend.appointment.mapper;

import com.gdc.backend.appointment.dto.AppointmentDayItemResponse;
import com.gdc.backend.appointment.dto.AppointmentResponse;
import com.gdc.backend.appointment.dto.DoctorBusySlotResponse;
import com.gdc.backend.appointment.entity.Appointment;
import com.gdc.backend.appointment.entity.DoctorBusySlot;
import com.gdc.backend.patient.entity.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class AppointmentMapper {

    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Kolkata");

    public AppointmentResponse toResponse(Appointment appointment) {

        Patient patient = appointment.getPatient();
        LocalDate date = toLocalDate(appointment);
        LocalTime time = toLocalTime(appointment);

        return new AppointmentResponse(
                appointment.getAppointmentId(),
                patient.getPatientId(),
                buildFullName(patient),
                patient.getMobile(),
                date,
                time,
                appointment.getAppointmentDateTime(),
                appointment.getAppointmentType(),
                appointment.getNotes(),
                appointment.getSystolicBp(),
                appointment.getDiastolicBp(),
                appointment.getHeartRate(),
                appointment.getBreathingRate(),
                appointment.getHeightCm(),
                appointment.getWeightKg(),
                appointment.getBmi(),
                appointment.getFbs(),
                appointment.getRbs(),
                appointment.isHasDiabetes(),
                appointment.isHasHypertension(),
                appointment.isHasHeartDisease(),
                appointment.isHasAsthma(),
                appointment.isHasKidneyDisease(),
                appointment.isHasLiverDisease(),
                appointment.isHasThyroidDisorder(),
                appointment.isHasBleedingDisorders(),
                appointment.isHasNeurologicalIssues(),
                appointment.isHasDrugAllergy(),
                appointment.isHasFoodAllergy(),
                appointment.isHasLatexAllergy(),
                appointment.getOtherAllergyNotes(),
                appointment.getPastSurgicalHistory(),
                appointment.isHasRootCanal(),
                appointment.isHasImplants(),
                appointment.isHasCrownsOrBridges(),
                appointment.isHasBraces(),
                appointment.isHasDentures(),
                appointment.getDentalComplicationNotes(),
                appointment.isConsentGiven(),
                appointment.getConsentReference(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getVersion()
        );
    }

    public AppointmentDayItemResponse toDayItem(Appointment appointment) {

        Patient patient = appointment.getPatient();

        return new AppointmentDayItemResponse(
                appointment.getAppointmentId(),
                toLocalTime(appointment),
                appointment.getAppointmentType(),
                patient.getPatientId(),
                buildFullName(patient),
                patient.getMobile(),
                appointment.getNotes()
        );
    }

    public DoctorBusySlotResponse toBusyResponse(DoctorBusySlot busySlot) {
        return new DoctorBusySlotResponse(
                busySlot.getId(),
                busySlot.getBusyDateTime()
                        .atZone(CLINIC_ZONE)
                        .toLocalTime(),
                busySlot.getNotes()
        );
    }

    private LocalDate toLocalDate(Appointment appointment) {
        return appointment.getAppointmentDateTime()
                .atZone(CLINIC_ZONE)
                .toLocalDate();
    }

    private LocalTime toLocalTime(Appointment appointment) {
        return appointment.getAppointmentDateTime()
                .atZone(CLINIC_ZONE)
                .toLocalTime();
    }

    private String buildFullName(Patient patient) {

        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            return patient.getFirstName();
        }

        return patient.getFirstName() + " " + patient.getLastName();
    }
}
