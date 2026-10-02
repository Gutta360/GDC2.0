package com.gdc.backend.pharmacy.controller;

import com.gdc.backend.pharmacy.dto.MedicineResponse;
import com.gdc.backend.pharmacy.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
