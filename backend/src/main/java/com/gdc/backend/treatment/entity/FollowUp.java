package com.gdc.backend.treatment.entity;

import com.gdc.backend.patient.entity.Patient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "follow_ups")
@EntityListeners(AuditingEntityListener.class)
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follow_up_id", nullable = false, unique = true, length = 30)
    private String followUpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_treatment_id")
    private Treatment relatedTreatment;

    @Column(name = "follow_up_date", nullable = false)
    private Instant followUpDate;

    @Column(name = "doctor_notes", length = 2000)
    private String doctorNotes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "followUp", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ClinicalProblem> newProblems = new ArrayList<>();

    @OneToMany(mappedBy = "followUp", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PrescriptionItem> prescriptionItems = new ArrayList<>();

    @OneToMany(mappedBy = "followUp", cascade = CascadeType.ALL, orphanRemoval = true)
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
    public String getFollowUpId() { return followUpId; }
    public void setFollowUpId(String followUpId) { this.followUpId = followUpId; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Treatment getRelatedTreatment() { return relatedTreatment; }
    public void setRelatedTreatment(Treatment relatedTreatment) { this.relatedTreatment = relatedTreatment; }
    public Instant getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Instant followUpDate) { this.followUpDate = followUpDate; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ClinicalProblem> getNewProblems() { return newProblems; }
    public List<PrescriptionItem> getPrescriptionItems() { return prescriptionItems; }
    public List<ClinicalScan> getScans() { return scans; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void addNewProblem(ClinicalProblem problem) {
        problem.setFollowUp(this);
        newProblems.add(problem);
    }

    public void addPrescriptionItem(PrescriptionItem item) {
        item.setFollowUp(this);
        prescriptionItems.add(item);
    }

    public void addScan(ClinicalScan scan) {
        scan.setFollowUp(this);
        scans.add(scan);
    }
}
