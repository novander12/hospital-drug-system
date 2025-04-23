package com.example.hospital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;

// DTO for creating a new Drug entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugCreateDTO {

    @NotBlank(message = "药品名称不能为空")
    private String name;

    @NotBlank(message = "药品规格不能为空")
    private String spec;

    @NotBlank(message = "药品类别不能为空")
    private String category;

    @NotBlank(message = "供应商不能为空")
    private String supplier;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
} 