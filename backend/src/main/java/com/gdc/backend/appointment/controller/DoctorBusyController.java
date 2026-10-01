package com.gdc.backend.appointment.controller;

import com.gdc.backend.appointment.dto.DoctorBusyCreateRequest;
import com.gdc.backend.appointment.dto.DoctorBusySlotResponse;
import com.gdc.backend.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/doctor-busy")
public class DoctorBusyController {

    private final AppointmentService appointmentService;

    public DoctorBusyController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<DoctorBusySlotResponse> createDoctorBusy(
            @Valid @RequestBody DoctorBusyCreateRequest request
    ) {

        DoctorBusySlotResponse response =
                appointmentService.createDoctorBusy(request);

        URI location = URI.create(
                "/api/v1/doctor-busy/" + response.id()
        );

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctorBusy(
            @PathVariable Long id
    ) {

        appointmentService.deleteDoctorBusy(id);
        return ResponseEntity.noContent().build();
    }
}
