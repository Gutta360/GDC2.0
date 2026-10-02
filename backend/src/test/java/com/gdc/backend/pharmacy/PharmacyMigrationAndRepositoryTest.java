package com.gdc.backend.pharmacy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PharmacyMigrationAndRepositoryTest {

    @Test
    void v4SyncsMedicineSequenceFromExistingMedicineIds() throws IOException {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V4__create_pharmacy_module.sql"));

        assertThat(migration).contains("CREATE SEQUENCE IF NOT EXISTS medicine_number_seq");
        assertThat(migration).contains("START WITH 1");
        assertThat(migration).doesNotContain("START WITH 4");
        assertThat(migration).contains("SELECT setval(");
        assertThat(migration).contains("MAX(SUBSTRING(medicine_id FROM 3)::INTEGER) + 1");
        assertThat(migration).contains("WHERE medicine_id ~ '^M-[0-9]{5}$'");
        assertThat(migration).contains("false");
    }

    @Test
    void pessimisticLockQueriesUseDeterministicOrder() throws IOException {
        String medicineRepository = Files.readString(Path.of(
                "src/main/java/com/gdc/backend/pharmacy/repository/MedicineRepository.java"
        ));
        String prescriptionRepository = Files.readString(Path.of(
                "src/main/java/com/gdc/backend/pharmacy/repository/PharmacyPrescriptionRepository.java"
        ));

        assertThat(medicineRepository).contains("ORDER BY m.id ASC");
        assertThat(prescriptionRepository).contains("ORDER BY pi.id ASC");
    }
}
