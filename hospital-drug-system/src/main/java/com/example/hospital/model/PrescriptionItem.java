package com.example.hospital.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonBackReference
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY) // Consider EAGER if drug info is always needed
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @NotNull(message = "药品数量不能为空")
    @Min(value = 1, message = "药品数量必须大于0")
    @Column(nullable = false)
    private Integer quantity; // 药品数量

    @Column(length = 100) 
    private String dosage; // 用法用量，例如 "每日三次，每次一片"

    @Column(length = 100)
    private String frequency; // 用药频率，例如 "饭后服用"

    @Column(length = 500)
    private String notes; // 其他注意事项

    // Constructors
    public PrescriptionItem() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 