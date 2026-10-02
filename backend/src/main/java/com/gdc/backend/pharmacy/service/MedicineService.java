package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.MedicineResponse;
import com.gdc.backend.pharmacy.dto.MedicineStockRequest;

import java.util.List;

public interface MedicineService {
    List<MedicineResponse> getActiveMedicines();

    List<MedicineResponse> getStockMedicines();

    MedicineResponse createMedicine(MedicineStockRequest request);

    MedicineResponse updateMedicine(String medicineId, MedicineStockRequest request);

    void deactivateMedicine(String medicineId);
}
