package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.MedicineResponse;
import com.gdc.backend.pharmacy.dto.MedicineStockRequest;
import com.gdc.backend.pharmacy.entity.Medicine;
import com.gdc.backend.pharmacy.entity.MedicineStockMovement;
import com.gdc.backend.pharmacy.entity.StockMovementType;
import com.gdc.backend.pharmacy.exception.PharmacyNotFoundException;
import com.gdc.backend.pharmacy.exception.PharmacyValidationException;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.pharmacy.repository.MedicineStockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private static final String MEDICINE_ID_PREFIX = "M-";

    private final MedicineRepository medicineRepository;
    private final MedicineStockMovementRepository stockMovementRepository;
    private final Clock clock;

    public MedicineServiceImpl(
            MedicineRepository medicineRepository,
            MedicineStockMovementRepository stockMovementRepository,
            Clock clock
    ) {
        this.medicineRepository = medicineRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getActiveMedicines() {
        return medicineRepository
                .findAllByActiveTrueAndExpiryDateGreaterThanEqualOrderByMedicineNameAsc(LocalDate.now(clock))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getStockMedicines() {
        return medicineRepository
                .findAllByActiveTrueOrderByMedicineNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MedicineResponse createMedicine(MedicineStockRequest request) {
        String medicineName = normalizeMedicineName(request.medicineName());
        Integer quantity = validateQuantity(request.quantity());
        LocalDate expiryDate = validateExpiryDate(request.expiryDate(), true);

        if (medicineRepository.existsActiveByNormalizedName(medicineName)) {
            throw new PharmacyValidationException("Medicine name already exists");
        }

        List<Medicine> inactiveMedicines = medicineRepository.findInactiveByNormalizedNameOrderByIdAsc(medicineName);
        if (!inactiveMedicines.isEmpty()) {
            return reactivateMedicine(inactiveMedicines.getFirst(), medicineName, quantity, expiryDate);
        }

        Medicine medicine = new Medicine();
        medicine.setMedicineId(generateMedicineId());
        medicine.setMedicineName(medicineName);
        medicine.setAvailableQuantity(quantity);
        medicine.setExpiryDate(expiryDate);
        medicine.setActive(true);

        Medicine saved = medicineRepository.saveAndFlush(medicine);
        if (quantity > 0) {
            stockMovementRepository.save(stockMovement(saved, StockMovementType.STOCK_ADD, quantity, null));
        }

        return toResponse(saved);
    }

    private MedicineResponse reactivateMedicine(
            Medicine medicine,
            String medicineName,
            Integer quantity,
            LocalDate expiryDate
    ) {
        int delta = quantity - medicine.getAvailableQuantity();

        medicine.setMedicineName(medicineName);
        medicine.setAvailableQuantity(quantity);
        medicine.setExpiryDate(expiryDate);
        medicine.setActive(true);

        Medicine saved = medicineRepository.saveAndFlush(medicine);
        if (delta != 0) {
            stockMovementRepository.save(stockMovement(saved, StockMovementType.STOCK_ADJUSTMENT, delta, null));
        }

        return toResponse(saved);
    }

    @Override
    public MedicineResponse updateMedicine(String medicineId, MedicineStockRequest request) {
        Medicine medicine = findMedicine(medicineId);
        String medicineName = normalizeMedicineName(request.medicineName());
        Integer quantity = validateQuantity(request.quantity());
        LocalDate expiryDate = validateExpiryDate(request.expiryDate(), false);

        if (medicineRepository.existsActiveByNormalizedNameAndMedicineIdNot(medicineName, medicine.getMedicineId())) {
            throw new PharmacyValidationException("Medicine name already exists");
        }

        int delta = quantity - medicine.getAvailableQuantity();

        medicine.setMedicineName(medicineName);
        medicine.setAvailableQuantity(quantity);
        medicine.setExpiryDate(expiryDate);
        medicine.setActive(true);

        Medicine saved = medicineRepository.saveAndFlush(medicine);
        if (delta != 0) {
            stockMovementRepository.save(stockMovement(saved, StockMovementType.STOCK_ADJUSTMENT, delta, null));
        }

        return toResponse(saved);
    }

    @Override
    public void deactivateMedicine(String medicineId) {
        Medicine medicine = findMedicine(medicineId);
        medicine.setActive(false);
        medicineRepository.saveAndFlush(medicine);
    }

    private Medicine findMedicine(String medicineId) {
        String normalizedMedicineId = normalizeMedicineId(medicineId);
        return medicineRepository
                .findByMedicineIdAndActiveTrue(normalizedMedicineId)
                .orElseThrow(() -> new PharmacyNotFoundException("Medicine not found: " + normalizedMedicineId));
    }

    private MedicineResponse toResponse(Medicine medicine) {
        return new MedicineResponse(
                medicine.getMedicineId(),
                medicine.getMedicineName(),
                medicine.getAvailableQuantity(),
                medicine.getExpiryDate()
        );
    }

    private MedicineStockMovement stockMovement(
            Medicine medicine,
            StockMovementType movementType,
            Integer quantityDelta,
            String referenceId
    ) {
        MedicineStockMovement movement = new MedicineStockMovement();
        movement.setMedicine(medicine);
        movement.setMovementType(movementType);
        movement.setQuantityDelta(quantityDelta);
        movement.setReferenceType(referenceId == null ? null : "PHARMACY_PAYMENT");
        movement.setReferenceId(referenceId);
        return movement;
    }

    private String generateMedicineId() {
        return MEDICINE_ID_PREFIX + "%05d".formatted(medicineRepository.getNextMedicineNumber());
    }

    private String normalizeMedicineId(String medicineId) {
        if (medicineId == null || medicineId.isBlank()) {
            throw new PharmacyValidationException("Medicine ID is required");
        }

        return medicineId.trim().toUpperCase();
    }

    private String normalizeMedicineName(String medicineName) {
        if (medicineName == null || medicineName.isBlank()) {
            throw new PharmacyValidationException("Medicine name is required");
        }

        String normalized = medicineName.trim();
        if (normalized.length() > 150) {
            throw new PharmacyValidationException("Medicine name is too long");
        }

        return normalized;
    }

    private Integer validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new PharmacyValidationException("Quantity is required");
        }

        if (quantity < 0) {
            throw new PharmacyValidationException("Quantity cannot be negative");
        }

        return quantity;
    }

    private LocalDate validateExpiryDate(LocalDate expiryDate, boolean requireFuture) {
        if (expiryDate == null) {
            throw new PharmacyValidationException("Expiry date is required");
        }

        if (requireFuture && expiryDate.isBefore(LocalDate.now(clock))) {
            throw new PharmacyValidationException("Expiry date cannot be in the past");
        }

        return expiryDate;
    }
}
