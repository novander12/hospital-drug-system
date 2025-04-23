package com.example.hospital.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class InventoryAdjustRequest {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    @NotNull(message = "调整后的库存不能为空")
    @Min(value = 0, message = "调整后的库存不能为负数")
    private Integer newStock;

    private String remarks;

    // Getters and Setters
    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 