package com.example.hospital.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportItemDTO {
    private Long drugId;
    private String drugName;
    private String spec;
    private String category;
    private String supplier;
    private Integer totalStock;
} 