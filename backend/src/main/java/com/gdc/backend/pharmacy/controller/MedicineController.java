package com.gdc.backend.pharmacy.controller;

import com.gdc.backend.pharmacy.dto.MedicineResponse;
import com.gdc.backend.pharmacy.dto.MedicineStockRequest;
import com.gdc.backend.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getMedicines() {
        return ResponseEntity.ok(medicineService.getActiveMedicines());
    }

    @GetMapping("/stock")
    public ResponseEntity<List<MedicineResponse>> getStockMedicines() {
        return ResponseEntity.ok(medicineService.getStockMedicines());
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> createMedicine(
            @Valid @org.springframework.web.bind.annotation.RequestBody MedicineStockRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(medicineService.createMedicine(request));
    }

    @PutMapping("/{medicineId}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable String medicineId,
            @Valid @org.springframework.web.bind.annotation.RequestBody MedicineStockRequest request
    ) {
        return ResponseEntity.ok(medicineService.updateMedicine(medicineId, request));
    }

    @DeleteMapping("/{medicineId}")
    public ResponseEntity<Void> deactivateMedicine(
            @PathVariable String medicineId
    ) {
        medicineService.deactivateMedicine(medicineId);
        return ResponseEntity.noContent().build();
    }
}
