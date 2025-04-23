package com.example.hospital.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

// DTO for creating a new Drug Batch
public class DrugBatchCreateDTO {

    @NotBlank(message = "批号不能为空")
    private String batchNumber;

    @NotNull(message = "生产日期不能为空")
    private LocalDate productionDate;

    @NotNull(message = "有效期至不能为空")
    private LocalDate expirationDate;

    @NotNull(message = "批次数量不能为空")
    @Min(value = 1, message = "初始数量必须大于0") // Initial quantity should be positive
    private Integer quantity;

    private String supplier; // Optional, might default to Drug's supplier

    // Getters and Setters
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
} 