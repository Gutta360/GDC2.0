package com.gdc.backend.treatment.controller;

import com.gdc.backend.treatment.dto.FollowUpContextResponse;
import com.gdc.backend.treatment.dto.TreatmentCreateRequest;
import com.gdc.backend.treatment.dto.TreatmentResponse;
import com.gdc.backend.treatment.service.TreatmentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TreatmentResponse> createTreatment(
            @Valid @RequestPart("request") TreatmentCreateRequest request,
            @RequestPart(name = "scans", required = false) List<MultipartFile> scans
    ) {
        TreatmentResponse response = treatmentService.createTreatment(request, scans);

        return ResponseEntity
                .created(URI.create("/api/v1/treatments/" + response.treatmentId()))
                .body(response);
    }

    @GetMapping("/{treatmentId}")
    public ResponseEntity<TreatmentResponse> getTreatment(
            @PathVariable String treatmentId
    ) {
        return ResponseEntity.ok(treatmentService.getTreatment(treatmentId));
    }

    @GetMapping
    public ResponseEntity<List<TreatmentResponse>> getTreatments(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(treatmentService.getTreatments(patientId));
    }

    @GetMapping("/patients/{patientId}/latest")
    public ResponseEntity<TreatmentResponse> getLatestTreatmentForPatient(
            @PathVariable String patientId
    ) {
        return ResponseEntity.ok(treatmentService.getLatestTreatmentForPatient(patientId));
    }

    @GetMapping("/patients/{patientId}/follow-up-context")
    public ResponseEntity<FollowUpContextResponse> getFollowUpContext(
            @PathVariable String patientId
    ) {
        return ResponseEntity.ok(treatmentService.getFollowUpContext(patientId));
    }
}
