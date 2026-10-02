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

@Entity
@Table(name = "clinical_problem_teeth")
public class ClinicalProblemTooth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ClinicalProblem problem;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber;

    public Long getId() { return id; }
    public ClinicalProblem getProblem() { return problem; }
    public void setProblem(ClinicalProblem problem) { this.problem = problem; }
    public Integer getToothNumber() { return toothNumber; }
    public void setToothNumber(Integer toothNumber) { this.toothNumber = toothNumber; }
}
