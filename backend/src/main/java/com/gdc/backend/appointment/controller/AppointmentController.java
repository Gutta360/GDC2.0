package com.gdc.backend.appointment.controller;

import com.gdc.backend.appointment.dto.AppointmentAvailabilityResponse;
import com.gdc.backend.appointment.dto.AppointmentCalendarDayResponse;
import com.gdc.backend.appointment.dto.AppointmentCreateRequest;
import com.gdc.backend.appointment.dto.AppointmentDayResponse;
import com.gdc.backend.appointment.dto.AppointmentResponse;
import com.gdc.backend.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request
    ) {

        AppointmentResponse response =
                appointmentService.createAppointment(request);

        URI location = URI.create(
                "/api/v1/appointments/" + response.appointmentId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable String appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointment(appointmentId)
        );
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<AppointmentCalendarDayResponse>> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(
                appointmentService.getCalendar(from, to)
        );
    }

    @GetMapping("/day")
    public ResponseEntity<AppointmentDayResponse> getDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                appointmentService.getDay(date)
        );
    }

    @GetMapping("/availability")
    public ResponseEntity<AppointmentAvailabilityResponse> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(
                appointmentService.getAvailability(date)
        );
    }
}
