package com.example.hospital.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String patientName; // 患者姓名

    private String patientIdNumber; // 患者身份证号或其他唯一标识
    private Integer patientAge;     // 患者年龄
    private String patientGender;   // 患者性别

    @Column(nullable = false, name = "doctor_name")
    private String prescribingDoctor; // 开方医生

    @Column(nullable = false)
    private LocalDate prescriptionDate; // 开方日期

    @Column(length = 500)
    private String diagnosis; // 诊断信息

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status = PrescriptionStatus.PENDING; // 处方状态

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser; // 创建该记录的用户 (药师/护士等)

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 最后更新时间
    
    // 一对多关系：一张处方包含多个药品项
    // CascadeType.ALL: 对处方的操作（保存、删除）会级联到处方项
    // orphanRemoval = true: 当从处方的 items 列表中移除一个项时，该项也会从数据库中删除
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<PrescriptionItem> items = new ArrayList<>();

    public enum PrescriptionStatus {
        PENDING,    // 待审核/待处理
        APPROVED,   // 已审核/待发药
        DISPENSED,  // 已发药
        CANCELLED   // 已取消
    }

    // Helper method to add an item
    public void addItem(PrescriptionItem item) {
        items.add(item);
        item.setPrescription(this);
    }

    // Helper method to remove an item
    public void removeItem(PrescriptionItem item) {
        items.remove(item);
        item.setPrescription(null);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientIdNumber() {
        return patientIdNumber;
    }

    public void setPatientIdNumber(String patientIdNumber) {
        this.patientIdNumber = patientIdNumber;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPrescribingDoctor() {
        return prescribingDoctor;
    }

    public void setPrescribingDoctor(String prescribingDoctor) {
        this.prescribingDoctor = prescribingDoctor;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items;
    }
} 