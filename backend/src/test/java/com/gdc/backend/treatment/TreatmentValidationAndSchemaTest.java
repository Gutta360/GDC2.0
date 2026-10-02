package com.gdc.backend.treatment;

import com.gdc.backend.treatment.dto.PrescriptionItemRequest;
import com.gdc.backend.treatment.dto.TreatmentCreateRequest;
import com.gdc.backend.treatment.entity.ClinicalProblemTooth;
import com.gdc.backend.treatment.entity.RootCanalLength;
import com.gdc.backend.treatment.entity.TreatmentType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreatmentValidationAndSchemaTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void toothNumberMigrationColumnsMatchEntityIntegerFields() throws Exception {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V3__create_treatment_module.sql"));

        assertThat(migration).contains("tooth_number INTEGER NOT NULL");
        assertThat(migration).doesNotContain("tooth_number SMALLINT");
        assertThat(fieldType(ClinicalProblemTooth.class, "toothNumber")).isEqualTo(Integer.class);
        assertThat(fieldType(RootCanalLength.class, "toothNumber")).isEqualTo(Integer.class);
    }

    @Test
    void prescriptionQuantityIsRequiredAndPositive() {
        TreatmentCreateRequest missingQuantity = treatmentRequest(
                BigDecimal.valueOf(100),
                List.of(new PrescriptionItemRequest("M-00001", null))
        );
        TreatmentCreateRequest zeroQuantity = treatmentRequest(
                BigDecimal.valueOf(100),
                List.of(new PrescriptionItemRequest("M-00001", 0))
        );

        assertThat(validator.validate(missingQuantity))
                .anyMatch(violation -> violation.getMessage().contains("quantity is required"));
        assertThat(validator.validate(zeroQuantity))
                .anyMatch(violation -> violation.getMessage().contains("at least 1"));
    }

    @Test
    void treatmentAmountAllowsEightIntegerDigitsAndTwoDecimals() {
        assertThat(validator.validate(treatmentRequest(new BigDecimal("99999999.99"), List.of()))).isEmpty();
    }

    @Test
    void treatmentAmountRejectsMoreThanEightIntegerDigits() {
        assertThat(validator.validate(treatmentRequest(new BigDecimal("100000000.00"), List.of()))).isNotEmpty();
    }

    @Test
    void treatmentAmountRejectsMoreThanTwoDecimalPlaces() {
        assertThat(validator.validate(treatmentRequest(new BigDecimal("100.123"), List.of()))).isNotEmpty();
    }

    private TreatmentCreateRequest treatmentRequest(
            BigDecimal amount,
            List<PrescriptionItemRequest> medicines
    ) {
        return new TreatmentCreateRequest(
                "P-00001",
                Instant.parse("2026-10-02T00:00:00Z"),
                TreatmentType.ADVISED,
                amount,
                null,
                List.of(),
                medicines
        );
    }

    private Class<?> fieldType(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Field field = owner.getDeclaredField(fieldName);
        return field.getType();
    }
}
