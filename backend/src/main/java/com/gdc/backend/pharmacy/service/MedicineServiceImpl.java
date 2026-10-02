package com.gdc.backend.pharmacy.service;

import com.gdc.backend.pharmacy.dto.MedicineResponse;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public List<MedicineResponse> getActiveMedicines() {
        return medicineRepository
                .findAllByActiveTrueOrderByMedicineNameAsc()
                .stream()
                .map(medicine -> new MedicineResponse(
                        medicine.getMedicineId(),
                        medicine.getMedicineName(),
                        medicine.getAvailableQuantity()
                ))
                .toList();
    }
}
