package com.gdc.backend.treatment.mapper;

import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.treatment.dto.FollowUpResponse;
import com.gdc.backend.treatment.dto.ImplantDetailResponse;
import com.gdc.backend.treatment.dto.PrescriptionItemResponse;
import com.gdc.backend.treatment.dto.ProblemResponse;
import com.gdc.backend.treatment.dto.RootCanalLengthResponse;
import com.gdc.backend.treatment.dto.ScanResponse;
import com.gdc.backend.treatment.dto.TreatmentResponse;
import com.gdc.backend.treatment.entity.ClinicalProblem;
import com.gdc.backend.treatment.entity.ClinicalScan;
import com.gdc.backend.treatment.entity.FollowUp;
import com.gdc.backend.treatment.entity.ImplantDetail;
import com.gdc.backend.treatment.entity.PrescriptionItem;
import com.gdc.backend.treatment.entity.RootCanalLength;
import com.gdc.backend.treatment.entity.Treatment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TreatmentMapper {

    public TreatmentResponse toTreatmentResponse(Treatment treatment) {
        Patient patient = treatment.getPatient();

        return new TreatmentResponse(
                treatment.getTreatmentId(),
                patient.getPatientId(),
                fullName(patient),
                treatment.getTreatmentDate(),
                treatment.getTreatmentType(),
                treatment.getTreatmentAmount(),
                treatment.getDoctorNotes(),
                treatment.getProblems().stream().map(this::toProblemResponse).toList(),
                treatment.getPrescriptionItems().stream().map(this::toPrescriptionItemResponse).toList(),
                treatment.getScans().stream().map(this::toScanResponse).toList(),
                treatment.getCreatedAt(),
                treatment.getUpdatedAt(),
                treatment.getVersion()
        );
    }

    public FollowUpResponse toFollowUpResponse(FollowUp followUp) {
        Patient patient = followUp.getPatient();
        Treatment relatedTreatment = followUp.getRelatedTreatment();

        return new FollowUpResponse(
                followUp.getFollowUpId(),
                patient.getPatientId(),
                fullName(patient),
                relatedTreatment == null ? null : relatedTreatment.getTreatmentId(),
                followUp.getFollowUpDate(),
                followUp.getDoctorNotes(),
                followUp.getNewProblems().stream().map(this::toProblemResponse).toList(),
                followUp.getPrescriptionItems().stream().map(this::toPrescriptionItemResponse).toList(),
                followUp.getScans().stream().map(this::toScanResponse).toList(),
                followUp.getCreatedAt(),
                followUp.getUpdatedAt(),
                followUp.getVersion()
        );
    }

    private ProblemResponse toProblemResponse(ClinicalProblem problem) {
        ImplantDetail implantDetail = problem.getImplantDetail();

        return new ProblemResponse(
                problem.getProblemType(),
                problem.getTeeth()
                        .stream()
                        .map(tooth -> tooth.getToothNumber())
                        .sorted()
                        .toList(),
                problem.getNotes(),
                problem.getImpactionType(),
                implantDetail == null
                        ? null
                        : new ImplantDetailResponse(
                                implantDetail.getImplantType(),
                                implantDetail.getImplantWidthMm(),
                                implantDetail.getImplantLengthMm(),
                                implantDetail.getImplantCompany(),
                                implantDetail.getHealingCapPlacementDate(),
                                implantDetail.getAbutmentPlacementDate(),
                                implantDetail.getCrownPlacementDate()
                        ),
                problem.getRootCanalLengths()
                        .stream()
                        .sorted(Comparator
                                .comparing(RootCanalLength::getToothNumber)
                                .thenComparing(RootCanalLength::getCanalName))
                        .map(root -> new RootCanalLengthResponse(
                                root.getToothNumber(),
                                root.getCanalName(),
                                root.getLengthMm(),
                                root.getNotes()
                        ))
                        .toList()
        );
    }

    private PrescriptionItemResponse toPrescriptionItemResponse(PrescriptionItem item) {
        Medicine medicine = item.getMedicine();

        return new PrescriptionItemResponse(
                medicine.getMedicineId(),
                medicine.getMedicineName(),
                item.getQuantity(),
                medicine.getAvailableQuantity()
        );
    }

    private ScanResponse toScanResponse(ClinicalScan scan) {
        return new ScanResponse(
                scan.getId(),
                scan.getOriginalFilename(),
                scan.getContentType(),
                scan.getFileSizeBytes(),
                scan.getCreatedAt()
        );
    }

    private String fullName(Patient patient) {
        List<String> parts = List.of(
                patient.getFirstName() == null ? "" : patient.getFirstName().trim(),
                patient.getLastName() == null ? "" : patient.getLastName().trim()
        );

        String name = String.join(" ", parts).trim();
        return name.isBlank() ? patient.getPatientId() : name;
    }
}
