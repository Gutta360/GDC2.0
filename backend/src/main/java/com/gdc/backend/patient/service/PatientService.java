package com.gdc.backend.patient.service;

import java.util.List;

import com.gdc.backend.patient.dto.PatientCreateRequest;
import com.gdc.backend.patient.dto.PatientResponse;
import com.gdc.backend.patient.dto.PatientUpdateRequest;

public interface PatientService {

    PatientResponse createPatient(PatientCreateRequest request);

    PatientResponse getPatient(String patientId);

    List<PatientResponse> getAllPatients();

    PatientResponse updatePatient(
            String patientId,
            PatientUpdateRequest request
    );

    void deletePatient(String patientId);
}