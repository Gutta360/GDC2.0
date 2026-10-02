package com.gdc.backend.treatment.repository;

import com.gdc.backend.treatment.entity.FollowUp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    @EntityGraph(attributePaths = {"patient", "relatedTreatment"})
    Optional<FollowUp> findByFollowUpIdAndActiveTrue(String followUpId);

    @EntityGraph(attributePaths = {"patient", "relatedTreatment"})
    List<FollowUp> findAllByPatientPatientIdAndActiveTrueOrderByFollowUpDateDesc(String patientId);

    @Query(value = "SELECT nextval('follow_up_number_seq')", nativeQuery = true)
    Long getNextFollowUpNumber();

    @EntityGraph(attributePaths = {"patient", "relatedTreatment"})
    @Query("""
            SELECT f
            FROM FollowUp f
            WHERE f.patient.patientId = :patientId
              AND f.active = true
            ORDER BY f.followUpDate DESC, f.id DESC
            """)
    List<FollowUp> findHistoryForPatient(@Param("patientId") String patientId);
}
