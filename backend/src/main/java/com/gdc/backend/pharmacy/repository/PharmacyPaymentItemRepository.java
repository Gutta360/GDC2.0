package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.pharmacy.entity.PharmacyPaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyPaymentItemRepository extends JpaRepository<PharmacyPaymentItem, Long> {

    boolean existsByPrescriptionItemId(Long prescriptionItemId);
}
