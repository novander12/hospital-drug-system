package com.example.hospital.model;

import java.time.LocalDateTime;
import java.util.List;

public class DispensingRecord {
    private Long id;
    private Long prescriptionId;
    private String patientName;
    private String patientId;
    private String dispensedBy;
    private LocalDateTime dispensedTime;
    private List<PrescriptionDrug> drugs;
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDispensedBy() {
        return dispensedBy;
    }

    public void setDispensedBy(String dispensedBy) {
        this.dispensedBy = dispensedBy;
    }

    public LocalDateTime getDispensedTime() {
        return dispensedTime;
    }

    public void setDispensedTime(LocalDateTime dispensedTime) {
        this.dispensedTime = dispensedTime;
    }

    public List<PrescriptionDrug> getDrugs() {
        return drugs;
    }

    public void setDrugs(List<PrescriptionDrug> drugs) {
        this.drugs = drugs;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 