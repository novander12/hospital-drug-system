package com.example.hospital.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String action;
    private Long drugId;
    private String drugName;
    private LocalDateTime timestamp;
    private String details;
    
    // 构造函数
    public OperationLog() {
    }
    
    public OperationLog(String username, String action, Long drugId, String drugName, String details) {
        this.username = username;
        this.action = action;
        this.drugId = drugId;
        this.drugName = drugName;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Long getDrugId() {
        return drugId;
    }
    
    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }
    
    public String getDrugName() {
        return drugName;
    }
    
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
} 