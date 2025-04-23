package com.example.hospital.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Future;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "药品名称不能为空")
    private String name;
    
    @Column(nullable = false)
    private String spec;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String supplier;
    
    // ADD OneToMany relationship to DrugBatch
    @OneToMany(mappedBy = "drug", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference // To prevent serialization loop with DrugBatch
    private List<DrugBatch> batches = new ArrayList<>();
    
    // 添加无参构造函数 (JPA 需要)
    public Drug() {
    }
    
    // 添加带所有参数的构造函数
    public Drug(String name, String spec, String supplier, String category, Integer stock, String unit, Double price, LocalDate expirationDate) {
        this.name = name;
        this.spec = spec;
        this.supplier = supplier;
        this.category = category;
        // 注意: 'unit' 和 'price' 在当前模型中不存在，已省略
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // ADD getter/setter for batches
    public List<DrugBatch> getBatches() {
        return batches;
    }

    public void setBatches(List<DrugBatch> batches) {
        this.batches = batches;
    }

    // Helper method to add a batch
    public void addBatch(DrugBatch batch) {
        batches.add(batch);
        batch.setDrug(this);
    }

    // Helper method to remove a batch
    public void removeBatch(DrugBatch batch) {
        batches.remove(batch);
        batch.setDrug(null);
    }
    
    // Optional: Method to calculate total stock from batches
    // This might be better placed in a DTO or service layer
    @Transient // Mark as not persistent if added here
    public Integer getTotalStock() {
        if (batches == null) {
            return 0;
        }
        return batches.stream()
                      .mapToInt(DrugBatch::getQuantity)
                      .sum();
    }
} 