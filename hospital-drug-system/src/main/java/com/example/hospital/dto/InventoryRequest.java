package com.example.hospital.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class InventoryRequest {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须为正数")
    private Integer quantity;

    private String remarks;

    // Getters and Setters
    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 