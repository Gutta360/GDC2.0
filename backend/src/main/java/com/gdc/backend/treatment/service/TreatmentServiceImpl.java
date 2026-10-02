package com.gdc.backend.treatment.service;

import com.gdc.backend.patient.entity.Patient;
import com.gdc.backend.patient.exception.PatientNotFoundException;
import com.gdc.backend.patient.repository.PatientRepository;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.treatment.dto.FollowUpContextResponse;
import com.gdc.backend.treatment.dto.FollowUpCreateRequest;
import com.gdc.backend.treatment.dto.FollowUpResponse;
import com.gdc.backend.treatment.dto.ImplantDetailRequest;
import com.gdc.backend.treatment.dto.PrescriptionItemRequest;
import com.gdc.backend.treatment.dto.ProblemRequest;
import com.gdc.backend.treatment.dto.RootCanalLengthRequest;
import com.gdc.backend.treatment.dto.TreatmentCreateRequest;
import com.gdc.backend.treatment.dto.TreatmentResponse;
import com.gdc.backend.treatment.entity.ClinicalProblem;
import com.gdc.backend.treatment.entity.FollowUp;
import com.gdc.backend.treatment.entity.ImplantDetail;
import com.gdc.backend.treatment.entity.PrescriptionItem;
import com.gdc.backend.treatment.entity.ProblemType;
import com.gdc.backend.treatment.entity.RootCanalLength;
import com.gdc.backend.treatment.entity.Treatment;
import com.gdc.backend.treatment.exception.FollowUpNotFoundException;
import com.gdc.backend.treatment.exception.TreatmentNotFoundException;
import com.gdc.backend.treatment.exception.TreatmentValidationException;
import com.gdc.backend.treatment.mapper.TreatmentMapper;
import com.gdc.backend.treatment.repository.FollowUpRepository;
import com.gdc.backend.treatment.repository.TreatmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TreatmentServiceImpl implements TreatmentService {

    private static final String TREATMENT_ID_PREFIX = "T-";
    private static final String FOLLOW_UP_ID_PREFIX = "F-";

    private static final Set<Integer> VALID_TEETH = Set.of(
            18, 17, 16, 15, 14, 13, 12, 11,
            21, 22, 23, 24, 25, 26, 27, 28,
            48, 47, 46, 45, 44, 43, 42, 41,
            31, 32, 33, 34, 35, 36, 37, 38
    );

    private static final Map<Integer, Set<String>> CANALS_BY_TOOTH = Map.ofEntries(
            Map.entry(11, Set.of("Single")),
            Map.entry(21, Set.of("Single")),
            Map.entry(31, Set.of("Single")),
            Map.entry(41, Set.of("Single")),
            Map.entry(12, Set.of("Single")),
            Map.entry(22, Set.of("Single")),
            Map.entry(32, Set.of("Single")),
            Map.entry(42, Set.of("Single")),
            Map.entry(13, Set.of("Single")),
            Map.entry(23, Set.of("Single")),
            Map.entry(33, Set.of("Single")),
            Map.entry(43, Set.of("Single")),
            Map.entry(14, Set.of("Buccal", "Palatal")),
            Map.entry(24, Set.of("Buccal", "Palatal")),
            Map.entry(15, Set.of("Buccal", "Palatal")),
            Map.entry(25, Set.of("Buccal", "Palatal")),
            Map.entry(34, Set.of("Buccal", "Lingual")),
            Map.entry(44, Set.of("Buccal", "Lingual")),
            Map.entry(35, Set.of("Buccal", "Lingual")),
            Map.entry(45, Set.of("Buccal", "Lingual")),
            Map.entry(16, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(26, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(17, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(27, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(18, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(28, Set.of("Palatal", "Mesial", "Distal")),
            Map.entry(36, Set.of("Mesial", "Distal", "Lingual", "Distal 2")),
            Map.entry(46, Set.of("Mesial", "Distal", "Lingual", "Distal 2")),
            Map.entry(37, Set.of("Mesial", "Distal", "Lingual", "Distal 2")),
            Map.entry(47, Set.of("Mesial", "Distal", "Lingual", "Distal 2")),
            Map.entry(38, Set.of("Mesial", "Distal", "Lingual", "Distal 2")),
            Map.entry(48, Set.of("Mesial", "Distal", "Lingual", "Distal 2"))
    );

    private final PatientRepository patientRepository;
    private final MedicineRepository medicineRepository;
    private final TreatmentRepository treatmentRepository;
    private final FollowUpRepository followUpRepository;
    private final TreatmentMapper treatmentMapper;
    private final ScanStorageService scanStorageService;
    private final Clock clock;

    public TreatmentServiceImpl(
            PatientRepository patientRepository,
            MedicineRepository medicineRepository,
            TreatmentRepository treatmentRepository,
            FollowUpRepository followUpRepository,
            TreatmentMapper treatmentMapper,
            ScanStorageService scanStorageService,
            Clock clock
    ) {
        this.patientRepository = patientRepository;
        this.medicineRepository = medicineRepository;
        this.treatmentRepository = treatmentRepository;
        this.followUpRepository = followUpRepository;
        this.treatmentMapper = treatmentMapper;
        this.scanStorageService = scanStorageService;
        this.clock = clock;
    }

    @Override
    public TreatmentResponse createTreatment(
            TreatmentCreateRequest request,
            List<MultipartFile> scans
    ) {
        Patient patient = findActivePatient(request.patientId());

        Treatment treatment = new Treatment();
        treatment.setTreatmentId(generateTreatmentId());
        treatment.setPatient(patient);
        treatment.setTreatmentDate(request.treatmentDate());
        treatment.setTreatmentType(request.treatmentType());
        treatment.setTreatmentAmount(request.treatmentAmount());
        treatment.setDoctorNotes(normalizeOptionalText(request.doctorNotes()));

        toProblems(request.problems()).forEach(treatment::addProblem);
        toPrescriptionItems(request.prescribedMedicines()).forEach(treatment::addPrescriptionItem);
        StoredScanFiles storedScans = trackStoredScans();

        try {
            toScans(scans, storedScans).forEach(treatment::addScan);
            return treatmentMapper.toTreatmentResponse(treatmentRepository.saveAndFlush(treatment));
        } catch (RuntimeException exception) {
            storedScans.cleanupIfNoTransaction();
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TreatmentResponse getTreatment(String treatmentId) {
        return treatmentMapper.toTreatmentResponse(findTreatment(treatmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponse> getTreatments(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        return treatmentRepository
                .findAllByPatientPatientIdAndActiveTrueOrderByTreatmentDateDesc(normalizedPatientId)
                .stream()
                .map(treatmentMapper::toTreatmentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TreatmentResponse getLatestTreatmentForPatient(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        Treatment treatment = treatmentRepository
                .findFirstByPatientPatientIdAndActiveTrueOrderByTreatmentDateDescIdDesc(normalizedPatientId)
                .orElseThrow(() -> new TreatmentNotFoundException(normalizedPatientId));
        return treatmentMapper.toTreatmentResponse(treatment);
    }

    @Override
    public FollowUpResponse createFollowUp(
            FollowUpCreateRequest request,
            List<MultipartFile> scans
    ) {
        Patient patient = findActivePatient(request.patientId());

        FollowUp followUp = new FollowUp();
        followUp.setFollowUpId(generateFollowUpId());
        followUp.setPatient(patient);
        followUp.setFollowUpDate(request.followUpDate());
        followUp.setDoctorNotes(normalizeOptionalText(request.doctorNotes()));

        String relatedTreatmentId = normalizeOptionalText(request.relatedTreatmentId());
        if (relatedTreatmentId != null) {
            followUp.setRelatedTreatment(findTreatment(relatedTreatmentId));
        } else {
            treatmentRepository
                    .findFirstByPatientPatientIdAndActiveTrueOrderByTreatmentDateDescIdDesc(patient.getPatientId())
                    .ifPresent(followUp::setRelatedTreatment);
        }

        toProblems(request.newProblems()).forEach(followUp::addNewProblem);
        toPrescriptionItems(request.prescribedMedicines()).forEach(followUp::addPrescriptionItem);
        StoredScanFiles storedScans = trackStoredScans();

        try {
            toScans(scans, storedScans).forEach(followUp::addScan);
            return treatmentMapper.toFollowUpResponse(followUpRepository.saveAndFlush(followUp));
        } catch (RuntimeException exception) {
            storedScans.cleanupIfNoTransaction();
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUpResponse getFollowUp(String followUpId) {
        return treatmentMapper.toFollowUpResponse(findFollowUp(followUpId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUpResponse> getFollowUps(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        return followUpRepository
                .findAllByPatientPatientIdAndActiveTrueOrderByFollowUpDateDesc(normalizedPatientId)
                .stream()
                .map(treatmentMapper::toFollowUpResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUpContextResponse getFollowUpContext(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        TreatmentResponse latestTreatment = treatmentRepository
                .findFirstByPatientPatientIdAndActiveTrueOrderByTreatmentDateDescIdDesc(normalizedPatientId)
                .map(treatmentMapper::toTreatmentResponse)
                .orElse(null);

        List<FollowUpResponse> previousFollowUps = followUpRepository
                .findHistoryForPatient(normalizedPatientId)
                .stream()
                .map(treatmentMapper::toFollowUpResponse)
                .toList();

        return new FollowUpContextResponse(latestTreatment, previousFollowUps);
    }

    private List<ClinicalProblem> toProblems(List<ProblemRequest> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream().map(this::toProblem).toList();
    }

    private ClinicalProblem toProblem(ProblemRequest request) {
        validateProblem(request);

        ClinicalProblem problem = new ClinicalProblem();
        problem.setProblemType(request.problemType());
        problem.setNotes(normalizeOptionalText(request.notes()));
        problem.setImpactionType(request.impactionType());

        request.teeth()
                .stream()
                .distinct()
                .sorted()
                .forEach(problem::addTooth);

        if (request.problemType() == ProblemType.ROOT_CANAL && request.rootCanalLengths() != null) {
            request.rootCanalLengths()
                    .stream()
                    .filter(root -> root.lengthMm() != null || normalizeOptionalText(root.notes()) != null)
                    .forEach(root -> problem.addRootCanalLength(toRootCanalLength(root)));
        }

        if (request.problemType() == ProblemType.IMPLANTS && request.implantDetail() != null) {
            problem.setImplantDetail(toImplantDetail(request.implantDetail()));
        }

        return problem;
    }

    private RootCanalLength toRootCanalLength(RootCanalLengthRequest request) {
        RootCanalLength root = new RootCanalLength();
        root.setToothNumber(request.toothNumber());
        root.setCanalName(request.canalName().trim());
        root.setLengthMm(request.lengthMm());
        root.setNotes(normalizeOptionalText(request.notes()));
        return root;
    }

    private ImplantDetail toImplantDetail(ImplantDetailRequest request) {
        ImplantDetail detail = new ImplantDetail();
        detail.setImplantType(request.implantType());
        detail.setImplantWidthMm(request.implantWidthMm());
        detail.setImplantLengthMm(request.implantLengthMm());
        detail.setImplantCompany(normalizeOptionalText(request.implantCompany()));
        detail.setHealingCapPlacementDate(request.healingCapPlacementDate());
        detail.setAbutmentPlacementDate(request.abutmentPlacementDate());
        detail.setCrownPlacementDate(request.crownPlacementDate());
        return detail;
    }

    private List<PrescriptionItem> toPrescriptionItems(List<PrescriptionItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> quantitiesByMedicine = requests
                .stream()
                .collect(Collectors.toMap(
                        request -> normalizeMedicineId(request.medicineId()),
                        request -> validateMedicineQuantity(request.quantity()),
                        Integer::sum
                ));

        List<PrescriptionItem> items = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : quantitiesByMedicine.entrySet()) {
            Medicine medicine = medicineRepository
                    .findByMedicineIdAndActiveTrue(entry.getKey())
                    .orElseThrow(() -> new TreatmentValidationException("Medicine not found: " + entry.getKey()));

            Integer quantity = entry.getValue();

            if (medicine.getExpiryDate().isBefore(LocalDate.now(clock))) {
                throw new TreatmentValidationException("Medicine is expired: " + medicine.getMedicineName());
            }

            if (quantity > medicine.getAvailableQuantity()) {
                throw new TreatmentValidationException(
                        "Only " + medicine.getAvailableQuantity() + " quantity available for " + medicine.getMedicineName()
                );
            }

            PrescriptionItem item = new PrescriptionItem();
            item.setMedicine(medicine);
            item.setQuantity(quantity);
            items.add(item);
        }

        return items;
    }

    private List<com.gdc.backend.treatment.entity.ClinicalScan> toScans(
            List<MultipartFile> files,
            StoredScanFiles storedScans
    ) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .filter(file -> file != null)
                .map(file -> storeScan(file, storedScans))
                .toList();
    }

    private com.gdc.backend.treatment.entity.ClinicalScan storeScan(
            MultipartFile file,
            StoredScanFiles storedScans
    ) {
        com.gdc.backend.treatment.entity.ClinicalScan scan = scanStorageService.store(file);
        storedScans.add(scan);
        return scan;
    }

    private Integer validateMedicineQuantity(Integer quantity) {
        if (quantity == null) {
            throw new TreatmentValidationException("Medicine quantity is required");
        }

        if (quantity < 1) {
            throw new TreatmentValidationException("Medicine quantity must be at least 1");
        }

        return quantity;
    }

    private StoredScanFiles trackStoredScans() {
        StoredScanFiles storedScans = new StoredScanFiles();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        storedScans.cleanup();
                    }
                }
            });
        }

        return storedScans;
    }

    private void validateProblem(ProblemRequest request) {
        if (request.problemType() == null) {
            throw new TreatmentValidationException("Problem type is required");
        }

        if (request.teeth() == null || request.teeth().isEmpty()) {
            throw new TreatmentValidationException("At least one tooth is required");
        }

        Set<Integer> uniqueTeeth = new HashSet<>(request.teeth());

        if (uniqueTeeth.size() != request.teeth().size()) {
            throw new TreatmentValidationException("Duplicate teeth are not allowed in a problem");
        }

        for (Integer tooth : request.teeth()) {
            if (tooth == null || !VALID_TEETH.contains(tooth)) {
                throw new TreatmentValidationException("Invalid FDI tooth number: " + tooth);
            }
        }

        if (request.problemType() == ProblemType.ROOT_CANAL) {
            validateRootCanal(request);
        }

        if (request.problemType() == ProblemType.IMPLANTS && request.implantDetail() == null) {
            throw new TreatmentValidationException("Implant details are required for implant problems");
        }

        if (request.problemType() == ProblemType.IMPACTION && request.impactionType() == null) {
            throw new TreatmentValidationException("Impaction type is required for impaction problems");
        }
    }

    private void validateRootCanal(ProblemRequest request) {
        if (request.rootCanalLengths() == null) {
            return;
        }

        Set<Integer> selectedTeeth = new HashSet<>(request.teeth());

        for (RootCanalLengthRequest root : request.rootCanalLengths()) {
            if (root.toothNumber() == null || !selectedTeeth.contains(root.toothNumber())) {
                throw new TreatmentValidationException("Root canal length references an unselected tooth");
            }

            String canalName = root.canalName() == null ? "" : root.canalName().trim();
            Set<String> validCanals = CANALS_BY_TOOTH.getOrDefault(root.toothNumber(), Set.of());

            if (!validCanals.contains(canalName) && !"Others".equals(canalName)) {
                throw new TreatmentValidationException("Invalid canal " + canalName + " for tooth " + root.toothNumber());
            }
        }
    }

    private Patient findActivePatient(String patientId) {
        String normalizedPatientId = normalizePatientId(patientId);
        return patientRepository
                .findByPatientIdAndActiveTrue(normalizedPatientId)
                .orElseThrow(() -> new PatientNotFoundException(normalizedPatientId));
    }

    private Treatment findTreatment(String treatmentId) {
        String normalizedTreatmentId = normalizeTreatmentId(treatmentId);
        return treatmentRepository
                .findByTreatmentIdAndActiveTrue(normalizedTreatmentId)
                .orElseThrow(() -> new TreatmentNotFoundException(normalizedTreatmentId));
    }

    private FollowUp findFollowUp(String followUpId) {
        String normalizedFollowUpId = normalizeFollowUpId(followUpId);
        return followUpRepository
                .findByFollowUpIdAndActiveTrue(normalizedFollowUpId)
                .orElseThrow(() -> new FollowUpNotFoundException(normalizedFollowUpId));
    }

    private String generateTreatmentId() {
        return TREATMENT_ID_PREFIX + "%05d".formatted(treatmentRepository.getNextTreatmentNumber());
    }

    private String generateFollowUpId() {
        return FOLLOW_UP_ID_PREFIX + "%05d".formatted(followUpRepository.getNextFollowUpNumber());
    }

    private String normalizePatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new TreatmentValidationException("Patient ID is required");
        }

        return patientId.trim().toUpperCase();
    }

    private String normalizeTreatmentId(String treatmentId) {
        if (treatmentId == null || treatmentId.isBlank()) {
            throw new TreatmentValidationException("Treatment ID is required");
        }

        return treatmentId.trim().toUpperCase();
    }

    private String normalizeFollowUpId(String followUpId) {
        if (followUpId == null || followUpId.isBlank()) {
            throw new TreatmentValidationException("Follow-up ID is required");
        }

        return followUpId.trim().toUpperCase();
    }

    private String normalizeMedicineId(String medicineId) {
        if (medicineId == null || medicineId.isBlank()) {
            throw new TreatmentValidationException("Medicine ID is required");
        }

        return medicineId.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private class StoredScanFiles {

        private final List<com.gdc.backend.treatment.entity.ClinicalScan> scans = new ArrayList<>();

        void add(com.gdc.backend.treatment.entity.ClinicalScan scan) {
            scans.add(scan);
        }

        void cleanupIfNoTransaction() {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                cleanup();
            }
        }

        void cleanup() {
            scans.forEach(scanStorageService::deleteStoredFile);
        }
    }
}
