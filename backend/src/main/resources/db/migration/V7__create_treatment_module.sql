-- Current Treatment/Pharmacy/Payment persistence was introduced in V6.
-- V7 tightens the database invariants to match the current Java services
-- and synchronizes human-readable ID sequences without rewriting V1-V6.

-- ------------------------------------------------------------
-- Treatment clinical invariants
-- ------------------------------------------------------------

ALTER TABLE clinical_problem_teeth
    ADD CONSTRAINT chk_clinical_problem_teeth_fdi
        CHECK (
            tooth_number IN (
                18, 17, 16, 15, 14, 13, 12, 11,
                21, 22, 23, 24, 25, 26, 27, 28,
                48, 47, 46, 45, 44, 43, 42, 41,
                31, 32, 33, 34, 35, 36, 37, 38
            )
        );

ALTER TABLE clinical_problem_teeth
    ADD CONSTRAINT uk_clinical_problem_teeth_problem_tooth
        UNIQUE (problem_id, tooth_number);

ALTER TABLE root_canal_lengths
    ADD CONSTRAINT chk_root_canal_lengths_fdi
        CHECK (
            tooth_number IN (
                18, 17, 16, 15, 14, 13, 12, 11,
                21, 22, 23, 24, 25, 26, 27, 28,
                48, 47, 46, 45, 44, 43, 42, 41,
                31, 32, 33, 34, 35, 36, 37, 38
            )
        );

ALTER TABLE root_canal_lengths
    ADD CONSTRAINT uk_root_canal_lengths_problem_tooth_canal
        UNIQUE (problem_id, tooth_number, canal_name);

ALTER TABLE clinical_problems
    ADD CONSTRAINT chk_clinical_problems_impaction_required
        CHECK (
            problem_type <> 'IMPACTION'
            OR impaction_type IS NOT NULL
        );

ALTER TABLE clinical_scans
    ADD CONSTRAINT chk_clinical_scans_file_size_positive
        CHECK (file_size_bytes > 0);

ALTER TABLE clinical_scans
    ADD CONSTRAINT chk_clinical_scans_content_type
        CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp'));

-- ------------------------------------------------------------
-- Pharmacy/payment invariants used by Treatment prescriptions
-- ------------------------------------------------------------

ALTER TABLE medicine_stock_movements
    ADD CONSTRAINT chk_medicine_stock_movements_quantity_delta
        CHECK (quantity_delta <> 0);

ALTER TABLE pharmacy_payment_items
    ADD CONSTRAINT chk_pharmacy_payment_items_full_fulfilment
        CHECK (dispensed_quantity = prescribed_quantity);

-- ------------------------------------------------------------
-- Sequence synchronization
-- ------------------------------------------------------------

SELECT setval(
    'treatment_number_seq',
    GREATEST(
        COALESCE(
            (
                SELECT MAX(SUBSTRING(treatment_id FROM 3)::BIGINT) + 1
                FROM treatments
                WHERE treatment_id ~ '^T-[0-9]{5}$'
            ),
            1
        ),
        1
    ),
    false
);

SELECT setval(
    'follow_up_number_seq',
    GREATEST(
        COALESCE(
            (
                SELECT MAX(SUBSTRING(follow_up_id FROM 3)::BIGINT) + 1
                FROM follow_ups
                WHERE follow_up_id ~ '^F-[0-9]{5}$'
            ),
            1
        ),
        1
    ),
    false
);

SELECT setval(
    'medicine_number_seq',
    GREATEST(
        COALESCE(
            (
                SELECT MAX(SUBSTRING(medicine_id FROM 3)::BIGINT) + 1
                FROM medicines
                WHERE medicine_id ~ '^M-[0-9]{5}$'
            ),
            1
        ),
        1
    ),
    false
);

SELECT setval(
    'pharmacy_payment_number_seq',
    GREATEST(
        COALESCE(
            (
                SELECT MAX(SUBSTRING(payment_id FROM 5)::BIGINT) + 1
                FROM pharmacy_payments
                WHERE payment_id ~ '^PAY-[0-9]{5}$'
            ),
            1
        ),
        1
    ),
    false
);

SELECT setval(
    'treatment_payment_number_seq',
    GREATEST(
        COALESCE(
            (
                SELECT MAX(SUBSTRING(payment_id FROM 6)::BIGINT) + 1
                FROM payments
                WHERE payment_id ~ '^TPAY-[0-9]{5}$'
            ),
            1
        ),
        1
    ),
    false
);
