package com.example.hospital.dto;

import com.example.hospital.model.Prescription;

import javax.validation.constraints.NotNull;

public class UpdateStatusRequestDTO {

    @NotNull(message = "新的处方状态不能为空")
    private Prescription.PrescriptionStatus newStatus;

    private String remarks;

    public Prescription.PrescriptionStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Prescription.PrescriptionStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
} 