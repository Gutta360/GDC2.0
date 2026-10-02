package com.gdc.backend.pharmacy.repository;

import com.gdc.backend.pharmacy.entity.Medicine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findAllByActiveTrueOrderByMedicineNameAsc();

    List<Medicine> findAllByActiveTrueAndExpiryDateGreaterThanEqualOrderByMedicineNameAsc(LocalDate expiryDate);

    Optional<Medicine> findByMedicineIdAndActiveTrue(String medicineId);

    Optional<Medicine> findByMedicineId(String medicineId);

    @Query("""
            SELECT m
            FROM Medicine m
            WHERE LOWER(TRIM(m.medicineName)) = LOWER(TRIM(:medicineName))
              AND m.active = false
            ORDER BY m.id ASC
            """)
    List<Medicine> findInactiveByNormalizedNameOrderByIdAsc(@Param("medicineName") String medicineName);

    @Query("""
            SELECT COUNT(m) > 0
            FROM Medicine m
            WHERE LOWER(TRIM(m.medicineName)) = LOWER(TRIM(:medicineName))
              AND m.active = true
            """)
    boolean existsActiveByNormalizedName(@Param("medicineName") String medicineName);

    @Query("""
            SELECT COUNT(m) > 0
            FROM Medicine m
            WHERE LOWER(TRIM(m.medicineName)) = LOWER(TRIM(:medicineName))
              AND m.medicineId <> :medicineId
              AND m.active = true
            """)
    boolean existsActiveByNormalizedNameAndMedicineIdNot(
            @Param("medicineName") String medicineName,
            @Param("medicineId") String medicineId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT m
            FROM Medicine m
            WHERE m.id IN :ids
            ORDER BY m.id ASC
            """)
    List<Medicine> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    @Query(value = "SELECT nextval('medicine_number_seq')", nativeQuery = true)
    Long getNextMedicineNumber();
}
