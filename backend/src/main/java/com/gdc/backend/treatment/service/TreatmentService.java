package com.gdc.backend.treatment.service;

import com.gdc.backend.treatment.dto.FollowUpContextResponse;
import com.gdc.backend.treatment.dto.FollowUpCreateRequest;
import com.gdc.backend.treatment.dto.FollowUpResponse;
import com.gdc.backend.treatment.dto.TreatmentCreateRequest;
import com.gdc.backend.treatment.dto.TreatmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TreatmentService {

    TreatmentResponse createTreatment(TreatmentCreateRequest request, List<MultipartFile> scans);

    TreatmentResponse getTreatment(String treatmentId);

    List<TreatmentResponse> getTreatments(String patientId);

    TreatmentResponse getLatestTreatmentForPatient(String patientId);

    FollowUpResponse createFollowUp(FollowUpCreateRequest request, List<MultipartFile> scans);

    FollowUpResponse getFollowUp(String followUpId);

    List<FollowUpResponse> getFollowUps(String patientId);

    FollowUpContextResponse getFollowUpContext(String patientId);
}
