package com.gdc.backend.pharmacy.entity;

import com.gdc.backend.treatment.entity.PrescriptionItem;
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
@Table(name = "pharmacy_payment_items")
public class PharmacyPaymentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PharmacyPayment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_item_id", nullable = false, unique = true)
    private PrescriptionItem prescriptionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "prescribed_quantity", nullable = false)
    private Integer prescribedQuantity;

    @Column(name = "dispensed_quantity", nullable = false)
    private Integer dispensedQuantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    public Long getId() { return id; }
    public PharmacyPayment getPayment() { return payment; }
    public void setPayment(PharmacyPayment payment) { this.payment = payment; }
    public PrescriptionItem getPrescriptionItem() { return prescriptionItem; }
    public void setPrescriptionItem(PrescriptionItem prescriptionItem) { this.prescriptionItem = prescriptionItem; }
    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }
    public Integer getPrescribedQuantity() { return prescribedQuantity; }
    public void setPrescribedQuantity(Integer prescribedQuantity) { this.prescribedQuantity = prescribedQuantity; }
    public Integer getDispensedQuantity() { return dispensedQuantity; }
    public void setDispensedQuantity(Integer dispensedQuantity) { this.dispensedQuantity = dispensedQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
