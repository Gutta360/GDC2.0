package com.gdc.backend.payment.repository;

import com.gdc.backend.payment.entity.TreatmentPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TreatmentPaymentRepository extends JpaRepository<TreatmentPayment, Long> {

    @EntityGraph(attributePaths = {"patient", "treatment"})
    Optional<TreatmentPayment> findByPaymentId(String paymentId);

    @EntityGraph(attributePaths = {"patient", "treatment"})
    List<TreatmentPayment> findAllByPatientPatientIdOrderByPaidAtDescIdDesc(String patientId);

    boolean existsByTreatmentTreatmentId(String treatmentId);

    @Query("""
            SELECT p
            FROM TreatmentPayment p
            JOIN FETCH p.patient patient
            JOIN FETCH p.treatment treatment
            WHERE treatment.id = :treatmentId
            """)
    Optional<TreatmentPayment> findByTreatmentId(@Param("treatmentId") Long treatmentId);

    @Query(value = "SELECT nextval('treatment_payment_number_seq')", nativeQuery = true)
    Long getNextTreatmentPaymentNumber();
}
