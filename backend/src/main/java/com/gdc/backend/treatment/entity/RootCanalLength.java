package com.gdc.backend.treatment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "root_canal_lengths")
public class RootCanalLength {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ClinicalProblem problem;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    @Column(name = "canal_name", nullable = false, length = 40)
    private String canalName;

    @Column(name = "length_mm", precision = 5, scale = 2)
    private BigDecimal lengthMm;

    @Column(name = "notes", length = 500)
    private String notes;

    public Long getId() { return id; }
    public ClinicalProblem getProblem() { return problem; }
    public void setProblem(ClinicalProblem problem) { this.problem = problem; }
    public Integer getToothNumber() { return toothNumber; }
    public void setToothNumber(Integer toothNumber) { this.toothNumber = toothNumber; }
    public String getCanalName() { return canalName; }
    public void setCanalName(String canalName) { this.canalName = canalName; }
    public BigDecimal getLengthMm() { return lengthMm; }
    public void setLengthMm(BigDecimal lengthMm) { this.lengthMm = lengthMm; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
