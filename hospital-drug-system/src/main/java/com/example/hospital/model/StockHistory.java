package com.example.hospital.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate date;
    private Integer totalStock;
    
    // 构造函数
    public StockHistory() {
    }
    
    public StockHistory(LocalDate date, Integer totalStock) {
        this.date = date;
        this.totalStock = totalStock;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Integer getTotalStock() {
        return totalStock;
    }
    
    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }
} 