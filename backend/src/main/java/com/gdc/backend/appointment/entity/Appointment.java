package com.gdc.backend.appointment.entity;

import com.gdc.backend.patient.entity.Patient;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "appointments")
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false, unique = true, length = 20)
    private String appointmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_reservation_id", nullable = false)
    private AppointmentSlotReservation slotReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "appointment_datetime", nullable = false)
    private Instant appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false, length = 20)
    private AppointmentType appointmentType;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "systolic_bp")
    private Short systolicBp;

    @Column(name = "diastolic_bp")
    private Short diastolicBp;

    @Column(name = "heart_rate")
    private Short heartRate;

    @Column(name = "breathing_rate")
    private Short breathingRate;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "bmi", precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "fbs", precision = 6, scale = 2)
    private BigDecimal fbs;

    @Column(name = "rbs", precision = 6, scale = 2)
    private BigDecimal rbs;

    @Column(name = "has_diabetes", nullable = false)
    private boolean hasDiabetes;

    @Column(name = "has_hypertension", nullable = false)
    private boolean hasHypertension;

    @Column(name = "has_heart_disease", nullable = false)
    private boolean hasHeartDisease;

    @Column(name = "has_asthma", nullable = false)
    private boolean hasAsthma;

    @Column(name = "has_kidney_disease", nullable = false)
    private boolean hasKidneyDisease;

    @Column(name = "has_liver_disease", nullable = false)
    private boolean hasLiverDisease;

    @Column(name = "has_thyroid_disorder", nullable = false)
    private boolean hasThyroidDisorder;

    @Column(name = "has_bleeding_disorders", nullable = false)
    private boolean hasBleedingDisorders;

    @Column(name = "has_neurological_issues", nullable = false)
    private boolean hasNeurologicalIssues;

    @Column(name = "has_drug_allergy", nullable = false)
    private boolean hasDrugAllergy;

    @Column(name = "has_food_allergy", nullable = false)
    private boolean hasFoodAllergy;

    @Column(name = "has_latex_allergy", nullable = false)
    private boolean hasLatexAllergy;

    @Column(name = "other_allergy_notes", length = 500)
    private String otherAllergyNotes;

    @Column(name = "past_surgical_history", length = 1000)
    private String pastSurgicalHistory;

    @Column(name = "has_root_canal", nullable = false)
    private boolean hasRootCanal;

    @Column(name = "has_implants", nullable = false)
    private boolean hasImplants;

    @Column(name = "has_crowns_or_bridges", nullable = false)
    private boolean hasCrownsOrBridges;

    @Column(name = "has_braces", nullable = false)
    private boolean hasBraces;

    @Column(name = "has_dentures", nullable = false)
    private boolean hasDentures;

    @Column(name = "dental_complication_notes", length = 1000)
    private String dentalComplicationNotes;

    @Column(name = "consent_given", nullable = false)
    private boolean consentGiven;

    @Column(name = "consent_reference", length = 255)
    private String consentReference;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

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
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public AppointmentSlotReservation getSlotReservation() { return slotReservation; }
    public void setSlotReservation(AppointmentSlotReservation slotReservation) { this.slotReservation = slotReservation; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Instant getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(Instant appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public AppointmentType getAppointmentType() { return appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Short getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Short systolicBp) { this.systolicBp = systolicBp; }
    public Short getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Short diastolicBp) { this.diastolicBp = diastolicBp; }
    public Short getHeartRate() { return heartRate; }
    public void setHeartRate(Short heartRate) { this.heartRate = heartRate; }
    public Short getBreathingRate() { return breathingRate; }
    public void setBreathingRate(Short breathingRate) { this.breathingRate = breathingRate; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }
    public BigDecimal getFbs() { return fbs; }
    public void setFbs(BigDecimal fbs) { this.fbs = fbs; }
    public BigDecimal getRbs() { return rbs; }
    public void setRbs(BigDecimal rbs) { this.rbs = rbs; }
    public boolean isHasDiabetes() { return hasDiabetes; }
    public void setHasDiabetes(boolean hasDiabetes) { this.hasDiabetes = hasDiabetes; }
    public boolean isHasHypertension() { return hasHypertension; }
    public void setHasHypertension(boolean hasHypertension) { this.hasHypertension = hasHypertension; }
    public boolean isHasHeartDisease() { return hasHeartDisease; }
    public void setHasHeartDisease(boolean hasHeartDisease) { this.hasHeartDisease = hasHeartDisease; }
    public boolean isHasAsthma() { return hasAsthma; }
    public void setHasAsthma(boolean hasAsthma) { this.hasAsthma = hasAsthma; }
    public boolean isHasKidneyDisease() { return hasKidneyDisease; }
    public void setHasKidneyDisease(boolean hasKidneyDisease) { this.hasKidneyDisease = hasKidneyDisease; }
    public boolean isHasLiverDisease() { return hasLiverDisease; }
    public void setHasLiverDisease(boolean hasLiverDisease) { this.hasLiverDisease = hasLiverDisease; }
    public boolean isHasThyroidDisorder() { return hasThyroidDisorder; }
    public void setHasThyroidDisorder(boolean hasThyroidDisorder) { this.hasThyroidDisorder = hasThyroidDisorder; }
    public boolean isHasBleedingDisorders() { return hasBleedingDisorders; }
    public void setHasBleedingDisorders(boolean hasBleedingDisorders) { this.hasBleedingDisorders = hasBleedingDisorders; }
    public boolean isHasNeurologicalIssues() { return hasNeurologicalIssues; }
    public void setHasNeurologicalIssues(boolean hasNeurologicalIssues) { this.hasNeurologicalIssues = hasNeurologicalIssues; }
    public boolean isHasDrugAllergy() { return hasDrugAllergy; }
    public void setHasDrugAllergy(boolean hasDrugAllergy) { this.hasDrugAllergy = hasDrugAllergy; }
    public boolean isHasFoodAllergy() { return hasFoodAllergy; }
    public void setHasFoodAllergy(boolean hasFoodAllergy) { this.hasFoodAllergy = hasFoodAllergy; }
    public boolean isHasLatexAllergy() { return hasLatexAllergy; }
    public void setHasLatexAllergy(boolean hasLatexAllergy) { this.hasLatexAllergy = hasLatexAllergy; }
    public String getOtherAllergyNotes() { return otherAllergyNotes; }
    public void setOtherAllergyNotes(String otherAllergyNotes) { this.otherAllergyNotes = otherAllergyNotes; }
    public String getPastSurgicalHistory() { return pastSurgicalHistory; }
    public void setPastSurgicalHistory(String pastSurgicalHistory) { this.pastSurgicalHistory = pastSurgicalHistory; }
    public boolean isHasRootCanal() { return hasRootCanal; }
    public void setHasRootCanal(boolean hasRootCanal) { this.hasRootCanal = hasRootCanal; }
    public boolean isHasImplants() { return hasImplants; }
    public void setHasImplants(boolean hasImplants) { this.hasImplants = hasImplants; }
    public boolean isHasCrownsOrBridges() { return hasCrownsOrBridges; }
    public void setHasCrownsOrBridges(boolean hasCrownsOrBridges) { this.hasCrownsOrBridges = hasCrownsOrBridges; }
    public boolean isHasBraces() { return hasBraces; }
    public void setHasBraces(boolean hasBraces) { this.hasBraces = hasBraces; }
    public boolean isHasDentures() { return hasDentures; }
    public void setHasDentures(boolean hasDentures) { this.hasDentures = hasDentures; }
    public String getDentalComplicationNotes() { return dentalComplicationNotes; }
    public void setDentalComplicationNotes(String dentalComplicationNotes) { this.dentalComplicationNotes = dentalComplicationNotes; }
    public boolean isConsentGiven() { return consentGiven; }
    public void setConsentGiven(boolean consentGiven) { this.consentGiven = consentGiven; }
    public String getConsentReference() { return consentReference; }
    public void setConsentReference(String consentReference) { this.consentReference = consentReference; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
