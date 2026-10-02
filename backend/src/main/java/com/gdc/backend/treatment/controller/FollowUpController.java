package com.gdc.backend.treatment.controller;

import com.gdc.backend.treatment.dto.FollowUpCreateRequest;
import com.gdc.backend.treatment.dto.FollowUpResponse;
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
@RequestMapping("/api/v1/follow-ups")
public class FollowUpController {

    private final TreatmentService treatmentService;

    public FollowUpController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FollowUpResponse> createFollowUp(
            @Valid @RequestPart("request") FollowUpCreateRequest request,
            @RequestPart(name = "scans", required = false) List<MultipartFile> scans
    ) {
        FollowUpResponse response = treatmentService.createFollowUp(request, scans);

        return ResponseEntity
                .created(URI.create("/api/v1/follow-ups/" + response.followUpId()))
                .body(response);
    }

    @GetMapping("/{followUpId}")
    public ResponseEntity<FollowUpResponse> getFollowUp(
            @PathVariable String followUpId
    ) {
        return ResponseEntity.ok(treatmentService.getFollowUp(followUpId));
    }

    @GetMapping
    public ResponseEntity<List<FollowUpResponse>> getFollowUps(
            @RequestParam String patientId
    ) {
        return ResponseEntity.ok(treatmentService.getFollowUps(patientId));
    }
}
