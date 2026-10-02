package com.gdc.backend.payment;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMigrationAndSchemaTest {

    @Test
    void v5CreatesClinicalPaymentsWithoutTouchingPharmacyPayments() throws IOException {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V5__create_payment_module.sql"));

        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS payments");
        assertThat(migration).contains("treatment_id BIGINT NOT NULL UNIQUE");
        assertThat(migration).contains("FOREIGN KEY (patient_id) REFERENCES patients (id)");
        assertThat(migration).contains("FOREIGN KEY (treatment_id) REFERENCES treatments (id)");
        assertThat(migration).contains("CHECK (payment_mode IN ('CASH', 'UPI'))");
        assertThat(migration).contains("CHECK (amount >= 0)");
        assertThat(migration).contains("treatment_payment_number_seq");
        assertThat(migration).contains("WHERE payment_id ~ '^TPAY-[0-9]{5}$'");
        assertThat(migration).doesNotContain("pharmacy_payment_items");
        assertThat(migration).doesNotContain("medicine_stock_movements");
    }
}
