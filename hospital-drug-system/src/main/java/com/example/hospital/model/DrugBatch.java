package com.example.hospital.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "drug_batches")
public class DrugBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    @JsonBackReference // To prevent serialization loop with Drug
    private Drug drug;

    @NotBlank(message = "批号不能为空")
    @Column(nullable = false)
    private String batchNumber; // 批号

    @NotNull(message = "生产日期不能为空")
    @Column(nullable = false)
    private LocalDate productionDate; // 生产日期

    @NotNull(message = "有效期至不能为空")
    @Column(nullable = false)
    private LocalDate expirationDate; // 有效期至

    @NotNull(message = "批次数量不能为空")
    @Min(value = 0, message = "数量不能为负") // Allow 0 quantity for adjustments/consumption
    @Column(nullable = false)
    private Integer quantity; // 当前批次数量

    private String supplier; // 该批次的供应商 (可以与药品主供应商不同)

    // Constructors
    public DrugBatch() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
} 