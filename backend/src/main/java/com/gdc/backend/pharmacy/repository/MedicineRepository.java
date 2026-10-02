package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findAllByActiveTrueOrderByMedicineNameAsc();

    Optional<Medicine> findByMedicineIdAndActiveTrue(String medicineId);
}
