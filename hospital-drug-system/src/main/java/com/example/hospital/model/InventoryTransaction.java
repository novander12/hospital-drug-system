package com.example.hospital.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INBOUND, OUTBOUND, ADJUSTMENT, INITIAL

    @Column(nullable = false)
    private Integer quantityChange; // 正数表示增加，负数表示减少

    @Column(nullable = false)
    private Integer stockAfterTransaction; // 交易后的库存量

    @Column(nullable = false)
    private LocalDateTime transactionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 操作用户，可以为 null（例如系统初始化）
    private User user;

    @Column(length = 500)
    private String remarks; // 备注，例如出库原因、盘点差异等

    // Enum for Transaction Type
    public enum TransactionType {
        INBOUND,    // 入库
        OUTBOUND,   // 出库
        ADJUSTMENT, // 盘点调整
        INITIAL     // 初始库存录入
    }

    // Constructors
    public InventoryTransaction() {
        this.transactionTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getStockAfterTransaction() {
        return stockAfterTransaction;
    }

    public void setStockAfterTransaction(Integer stockAfterTransaction) {
        this.stockAfterTransaction = stockAfterTransaction;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 