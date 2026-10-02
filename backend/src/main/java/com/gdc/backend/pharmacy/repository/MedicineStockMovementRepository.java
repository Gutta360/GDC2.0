package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.pharmacy.entity.MedicineStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineStockMovementRepository extends JpaRepository<MedicineStockMovement, Long> {
}
