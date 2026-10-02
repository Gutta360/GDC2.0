package com.gdc.backend.treatment.entity;

import com.gdc.backend.patient.entity.Patient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treatments")
@EntityListeners(AuditingEntityListener.class)
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "treatment_id", nullable = false, unique = true, length = 30)
    private String treatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "treatment_date", nullable = false)
    private Instant treatmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "treatment_type", nullable = false, length = 20)
    private TreatmentType treatmentType;

    @Column(name = "treatment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal treatmentAmount;

    @Column(name = "doctor_notes", length = 2000)
    private String doctorNotes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ClinicalProblem> problems = new ArrayList<>();

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ClinicalScan> scans = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Long getId() { return id; }
    public String getTreatmentId() { return treatmentId; }
    public void setTreatmentId(String treatmentId) { this.treatmentId = treatmentId; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Instant getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(Instant treatmentDate) { this.treatmentDate = treatmentDate; }
    public TreatmentType getTreatmentType() { return treatmentType; }
    public void setTreatmentType(TreatmentType treatmentType) { this.treatmentType = treatmentType; }
    public BigDecimal getTreatmentAmount() { return treatmentAmount; }
    public void setTreatmentAmount(BigDecimal treatmentAmount) { this.treatmentAmount = treatmentAmount; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ClinicalProblem> getProblems() { return problems; }
    public List<PrescriptionItem> getPrescriptionItems() { return prescriptionItems; }
    public List<ClinicalScan> getScans() { return scans; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void addProblem(ClinicalProblem problem) {
        problem.setTreatment(this);
        problems.add(problem);
    }

    public void addPrescriptionItem(PrescriptionItem item) {
        item.setTreatment(this);
        prescriptionItems.add(item);
    }

    public void addScan(ClinicalScan scan) {
        scan.setTreatment(this);
        scans.add(scan);
    }
}
