package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.MedicineResponse;

import java.util.List;

public interface MedicineService {
    List<MedicineResponse> getActiveMedicines();
}
