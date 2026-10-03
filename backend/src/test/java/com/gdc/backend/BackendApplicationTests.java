package com.gdc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gdc.backend.payment.repository.TreatmentPaymentRepository;
import com.gdc.backend.pharmacy.repository.MedicineRepository;
import com.gdc.backend.pharmacy.repository.PharmacyPrescriptionRepository;
import com.gdc.backend.support.PostgresIntegrationTest;
import com.gdc.backend.treatment.repository.FollowUpRepository;
import com.gdc.backend.treatment.repository.TreatmentRepository;

import static org.assertj.core.api.Assertions.assertThat;

class BackendApplicationTests extends PostgresIntegrationTest {

	@Autowired
	private TreatmentRepository treatmentRepository;

	@Autowired
	private FollowUpRepository followUpRepository;

	@Autowired
	private MedicineRepository medicineRepository;

	@Autowired
	private PharmacyPrescriptionRepository pharmacyPrescriptionRepository;

	@Autowired
	private TreatmentPaymentRepository treatmentPaymentRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void treatmentRelatedRepositoriesInitializeAfterFlywayMigration() {
		assertThat(treatmentRepository).isNotNull();
		assertThat(followUpRepository).isNotNull();
		assertThat(medicineRepository).isNotNull();
		assertThat(pharmacyPrescriptionRepository).isNotNull();
		assertThat(treatmentPaymentRepository).isNotNull();
	}

}
