package com.example.hospital.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor; // 如果需要

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp; // 需要导入

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs") // 添加 Table 注解
@Getter // 使用 Lombok
@Setter // 使用 Lombok
@NoArgsConstructor // JPA 需要无参构造函数
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp // 使用 CreationTimestamp 自动设置时间
    @Column(nullable = false, updatable = false, name = "operation_time") // 明确列名并设为不可更新
    private LocalDateTime timestamp; // 保留字段名 timestamp，但功能类似 operationTime

    @Column(nullable = false) // 添加 Column 注解
    private String username;

    @Column(nullable = false, name = "operation_type") // 明确列名
    private String action; // 保留字段名 action，但功能类似 operationType

    @Column(nullable = false) // 添加新字段
    private String operationResult; // 例如 SUCCESS, FAILURE

    @Lob // 添加 Lob 注解
    @Column(columnDefinition = "TEXT") // 明确类型
    private String details; // 保留字段名 details

    @Column // 保留 drugId
    private Long drugId;

    @Column // 保留 drugName
    private String drugName;

    // 可选：自定义构造函数用于方便创建日志
    public OperationLog(String username, String action, String operationResult, String details, Long drugId, String drugName) {
        this.username = username;
        this.action = action; // 对应 operationType
        this.operationResult = operationResult;
        this.details = details; // 对应 operationDetails
        this.drugId = drugId;
        this.drugName = drugName;
        // timestamp 会自动设置 by @CreationTimestamp
    }

    // 添加构造函数以支持 DataInitializer 设置特定时间戳
    public OperationLog(String username, String action, String operationResult, String details, Long drugId, String drugName, LocalDateTime timestamp) {
        this.username = username;
        this.action = action;
        this.operationResult = operationResult;
        this.details = details;
        this.drugId = drugId;
        this.drugName = drugName;
        this.timestamp = timestamp; // 手动设置时间戳
    }

    // 移除旧的手动 Getters/Setters，因为使用了 Lombok @Getter/@Setter
} 