package com.example.hospital.dto;

import java.util.List;

// DTO for displaying Drug information, including calculated total stock
public class DrugDTO {
    private Long id;
    private String name;
    private String spec;
    private String category;
    private String supplier;
    private Integer totalStock; // Calculated from batches
    // Optional: Include batch details if needed directly in the drug list
    // private List<DrugBatchDTO> batches;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }

    // public List<DrugBatchDTO> getBatches() { return batches; }
    // public void setBatches(List<DrugBatchDTO> batches) { this.batches = batches; }
} 