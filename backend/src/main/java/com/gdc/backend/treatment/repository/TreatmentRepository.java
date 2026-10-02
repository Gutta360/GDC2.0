package com.gdc.backend.treatment.repository;

import com.gdc.backend.treatment.entity.Treatment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    @EntityGraph(attributePaths = "patient")
    Optional<Treatment> findByTreatmentIdAndActiveTrue(String treatmentId);

    @EntityGraph(attributePaths = "patient")
    List<Treatment> findAllByPatientPatientIdAndActiveTrueOrderByTreatmentDateDesc(String patientId);

    @Query(value = "SELECT nextval('treatment_number_seq')", nativeQuery = true)
    Long getNextTreatmentNumber();

    @EntityGraph(attributePaths = "patient")
    Optional<Treatment> findFirstByPatientPatientIdAndActiveTrueOrderByTreatmentDateDescIdDesc(
            String patientId
    );
}
