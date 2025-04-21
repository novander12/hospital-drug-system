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

@Entity
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "药品名称不能为空")
    private String name;
    
    @NotBlank(message = "药品规格不能为空")
    private String spec;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    @NotNull(message = "有效期不能为空")
    private LocalDate expirationDate;
    
    private String supplier;
    private String category; // 药品类别，如：抗生素、解热镇痛、维生素等
    
    // 添加无参构造函数 (JPA 需要)
    public Drug() {
    }
    
    // 添加带所有参数的构造函数
    public Drug(String name, String spec, String supplier, String category, Integer stock, String unit, Double price, LocalDate expirationDate) {
        this.name = name;
        this.spec = spec;
        this.supplier = supplier;
        this.category = category;
        this.stock = stock;
        // 注意: 'unit' 和 'price' 在当前模型中不存在，已省略
        this.expirationDate = expirationDate;
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
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
} 