package com.example.hospital.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据传输对象，用于药品消耗统计报告
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugConsumptionDTO {
    private Long drugId;
    private String drugName;
    private String spec;
    private String category;
    // 使用 Long 以防消耗量非常大，或者根据实际情况调整为 Integer 或 BigDecimal
    private Long totalConsumedQuantity; 
} 