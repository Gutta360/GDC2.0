package com.gdc.backend.appointment.service;

import com.gdc.backend.appointment.dto.AppointmentAvailabilityResponse;
import com.gdc.backend.appointment.dto.AppointmentCalendarDayResponse;
import com.gdc.backend.appointment.dto.AppointmentCreateRequest;
import com.gdc.backend.appointment.dto.AppointmentDayResponse;
import com.gdc.backend.appointment.dto.AppointmentResponse;
import com.gdc.backend.appointment.dto.DoctorBusyCreateRequest;
import com.gdc.backend.appointment.dto.DoctorBusySlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentCreateRequest request);

    AppointmentResponse getAppointment(String appointmentId);

    AppointmentResponse getLatestForPatient(String patientId);

    List<AppointmentCalendarDayResponse> getCalendar(LocalDate from, LocalDate to);

    AppointmentDayResponse getDay(LocalDate date);

    AppointmentAvailabilityResponse getAvailability(LocalDate date);

    DoctorBusySlotResponse createDoctorBusy(DoctorBusyCreateRequest request);

    void deleteDoctorBusy(Long id);
}
