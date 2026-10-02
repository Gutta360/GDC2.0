package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.pharmacy.entity.PharmacyPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PharmacyPaymentRepository extends JpaRepository<PharmacyPayment, Long> {

    @EntityGraph(attributePaths = {"patient", "items", "items.medicine", "items.prescriptionItem"})
    Optional<PharmacyPayment> findByPaymentId(String paymentId);

    @EntityGraph(attributePaths = {"patient", "items", "items.medicine", "items.prescriptionItem"})
    List<PharmacyPayment> findAllByPatientPatientIdOrderByPaidAtDescIdDesc(String patientId);

    @Query(value = "SELECT nextval('pharmacy_payment_number_seq')", nativeQuery = true)
    Long getNextPaymentNumber();
}
