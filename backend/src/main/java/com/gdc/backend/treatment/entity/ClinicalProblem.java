package com.gdc.backend.treatment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clinical_problems")
public class ClinicalProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_up_id")
    private FollowUp followUp;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false, length = 40)
    private ProblemType problemType;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "impaction_type", length = 40)
    private ImpactionType impactionType;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("toothNumber ASC")
    private List<ClinicalProblemTooth> teeth = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("toothNumber ASC, canalName ASC")
    private List<RootCanalLength> rootCanalLengths = new ArrayList<>();

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private ImplantDetail implantDetail;

    public Long getId() { return id; }
    public Treatment getTreatment() { return treatment; }
    public void setTreatment(Treatment treatment) { this.treatment = treatment; }
    public FollowUp getFollowUp() { return followUp; }
    public void setFollowUp(FollowUp followUp) { this.followUp = followUp; }
    public ProblemType getProblemType() { return problemType; }
    public void setProblemType(ProblemType problemType) { this.problemType = problemType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ImpactionType getImpactionType() { return impactionType; }
    public void setImpactionType(ImpactionType impactionType) { this.impactionType = impactionType; }
    public List<ClinicalProblemTooth> getTeeth() { return teeth; }
    public List<RootCanalLength> getRootCanalLengths() { return rootCanalLengths; }
    public ImplantDetail getImplantDetail() { return implantDetail; }

    public void addTooth(int toothNumber) {
        ClinicalProblemTooth tooth = new ClinicalProblemTooth();
        tooth.setProblem(this);
        tooth.setToothNumber(toothNumber);
        teeth.add(tooth);
    }

    public void addRootCanalLength(RootCanalLength rootCanalLength) {
        rootCanalLength.setProblem(this);
        rootCanalLengths.add(rootCanalLength);
    }

    public void setImplantDetail(ImplantDetail implantDetail) {
        this.implantDetail = implantDetail;
        if (implantDetail != null) {
            implantDetail.setProblem(this);
        }
    }
}
