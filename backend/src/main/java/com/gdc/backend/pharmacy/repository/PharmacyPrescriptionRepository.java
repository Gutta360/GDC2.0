package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.treatment.entity.PrescriptionItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PharmacyPrescriptionRepository extends JpaRepository<PrescriptionItem, Long> {

    @Query("""
            SELECT pi
            FROM PrescriptionItem pi
            JOIN FETCH pi.medicine m
            LEFT JOIN FETCH pi.treatment t
            LEFT JOIN FETCH t.patient tp
            LEFT JOIN FETCH pi.followUp f
            LEFT JOIN FETCH f.patient fp
            WHERE (
                (t IS NOT NULL AND t.active = true AND tp.patientId = :patientId)
                OR (f IS NOT NULL AND f.active = true AND fp.patientId = :patientId)
            )
              AND NOT EXISTS (
                SELECT ppi.id
                FROM PharmacyPaymentItem ppi
                WHERE ppi.prescriptionItem = pi
              )
            ORDER BY COALESCE(t.treatmentDate, f.followUpDate) ASC, pi.id ASC
            """)
    List<PrescriptionItem> findPendingForPatient(@Param("patientId") String patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pi
            FROM PrescriptionItem pi
            JOIN FETCH pi.medicine m
            LEFT JOIN FETCH pi.treatment t
            LEFT JOIN FETCH t.patient tp
            LEFT JOIN FETCH pi.followUp f
            LEFT JOIN FETCH f.patient fp
            WHERE pi.id IN :ids
            ORDER BY pi.id ASC
            """)
    List<PrescriptionItem> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
