package com.gdc.backend.pharmacy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PharmacyMigrationAndRepositoryTest {

    @Test
    void v6CreatesMedicineFoundationAndV7SyncsMedicineSequence() throws IOException {
        String foundationMigration = Files.readString(Path.of(
                "src/main/resources/db/migration/V6__create_treatment_pharmacy_payment_tables.sql"
        ));
        String alignmentMigration = Files.readString(Path.of(
                "src/main/resources/db/migration/V7__create_treatment_module.sql"
        ));

        assertThat(foundationMigration).contains("CREATE SEQUENCE IF NOT EXISTS medicine_number_seq");
        assertThat(foundationMigration).contains("CREATE TABLE medicines");
        assertThat(foundationMigration).contains("expiry_date DATE NOT NULL");
        assertThat(foundationMigration).contains("START WITH 1");
        assertThat(foundationMigration).doesNotContain("START WITH 4");
        assertThat(alignmentMigration).contains("SELECT setval(");
        assertThat(alignmentMigration).contains("MAX(SUBSTRING(medicine_id FROM 3)::BIGINT) + 1");
        assertThat(alignmentMigration).contains("WHERE medicine_id ~ '^M-[0-9]{5}$'");
        assertThat(alignmentMigration).contains("false");
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
