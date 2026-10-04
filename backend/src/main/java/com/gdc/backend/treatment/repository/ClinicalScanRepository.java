package com.gdc.backend.treatment.repository;

import com.gdc.backend.treatment.entity.ClinicalScan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalScanRepository extends JpaRepository<ClinicalScan, Long> {
}
