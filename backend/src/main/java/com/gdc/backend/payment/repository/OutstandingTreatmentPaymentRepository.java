package com.gdc.backend.payment.repository;

import com.gdc.backend.treatment.entity.Treatment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutstandingTreatmentPaymentRepository extends JpaRepository<Treatment, Long> {

    @EntityGraph(attributePaths = "patient")
    @Query("""
            SELECT t
            FROM Treatment t
            WHERE t.patient.patientId = :patientId
              AND t.active = true
              AND NOT EXISTS (
                  SELECT p.id
                  FROM TreatmentPayment p
                  WHERE p.treatment = t
              )
            ORDER BY t.treatmentDate ASC, t.id ASC
            """)
    List<Treatment> findOutstandingTreatmentsForPatient(@Param("patientId") String patientId);
}
