package com.gdc.backend.treatment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "implant_details")
public class ImplantDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false, unique = true)
    private ClinicalProblem problem;

    @Enumerated(EnumType.STRING)
    @Column(name = "implant_type", length = 30)
    private ImplantType implantType;

    @Column(name = "implant_width_mm", precision = 5, scale = 2)
    private BigDecimal implantWidthMm;

    @Column(name = "implant_length_mm", precision = 5, scale = 2)
    private BigDecimal implantLengthMm;

    @Column(name = "implant_company", length = 150)
    private String implantCompany;

    @Column(name = "healing_cap_placement_date")
    private LocalDate healingCapPlacementDate;

    @Column(name = "abutment_placement_date")
    private LocalDate abutmentPlacementDate;

    @Column(name = "crown_placement_date")
    private LocalDate crownPlacementDate;

    public Long getId() { return id; }
    public ClinicalProblem getProblem() { return problem; }
    public void setProblem(ClinicalProblem problem) { this.problem = problem; }
    public ImplantType getImplantType() { return implantType; }
    public void setImplantType(ImplantType implantType) { this.implantType = implantType; }
    public BigDecimal getImplantWidthMm() { return implantWidthMm; }
    public void setImplantWidthMm(BigDecimal implantWidthMm) { this.implantWidthMm = implantWidthMm; }
    public BigDecimal getImplantLengthMm() { return implantLengthMm; }
    public void setImplantLengthMm(BigDecimal implantLengthMm) { this.implantLengthMm = implantLengthMm; }
    public String getImplantCompany() { return implantCompany; }
    public void setImplantCompany(String implantCompany) { this.implantCompany = implantCompany; }
    public LocalDate getHealingCapPlacementDate() { return healingCapPlacementDate; }
    public void setHealingCapPlacementDate(LocalDate healingCapPlacementDate) { this.healingCapPlacementDate = healingCapPlacementDate; }
    public LocalDate getAbutmentPlacementDate() { return abutmentPlacementDate; }
    public void setAbutmentPlacementDate(LocalDate abutmentPlacementDate) { this.abutmentPlacementDate = abutmentPlacementDate; }
    public LocalDate getCrownPlacementDate() { return crownPlacementDate; }
    public void setCrownPlacementDate(LocalDate crownPlacementDate) { this.crownPlacementDate = crownPlacementDate; }
}
