package com.gdc.backend.payment;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMigrationAndSchemaTest {

    @Test
    void v6CreatesClinicalPaymentsAndV7SyncsTreatmentPaymentSequence() throws IOException {
        String foundationMigration = Files.readString(Path.of(
                "src/main/resources/db/migration/V6__create_treatment_pharmacy_payment_tables.sql"
        ));
        String alignmentMigration = Files.readString(Path.of(
                "src/main/resources/db/migration/V7__create_treatment_module.sql"
        ));

        assertThat(foundationMigration).contains("CREATE TABLE payments");
        assertThat(foundationMigration).contains("treatment_id BIGINT NOT NULL");
        assertThat(foundationMigration).contains("CONSTRAINT uk_payments_treatment_id UNIQUE (treatment_id)");
        assertThat(foundationMigration).contains("CONSTRAINT fk_payments_patient FOREIGN KEY (patient_id)");
        assertThat(foundationMigration).contains("CONSTRAINT fk_payments_treatment FOREIGN KEY (treatment_id)");
        assertThat(foundationMigration).contains("CONSTRAINT chk_payments_mode CHECK (payment_mode IN ('CASH', 'UPI'))");
        assertThat(foundationMigration).contains("CONSTRAINT chk_payments_amount CHECK (amount >= 0.00)");
        assertThat(foundationMigration).contains("treatment_payment_number_seq");
        assertThat(alignmentMigration).contains("WHERE payment_id ~ '^TPAY-[0-9]{5}$'");
    }
}
