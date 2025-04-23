package com.example.hospital.dto;

import com.example.hospital.model.PrescriptionItem;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class PrescriptionCreateDTO {

    @NotBlank(message = "患者姓名不能为空")
    @Size(max = 100, message = "患者姓名过长")
    private String patientName;

    private String patientIdNumber;
    private Integer patientAge;
    private String patientGender;

    @NotBlank(message = "开方医生不能为空")
    @Size(max = 100, message = "医生姓名过长")
    private String prescribingDoctor;

    @NotNull(message = "开方日期不能为空")
    private LocalDate prescriptionDate;

    @Size(max = 500, message = "诊断信息过长")
    private String diagnosis;

    @NotEmpty(message = "处方药品项不能为空")
    @Valid // 确保对列表中的每个 DTO 也进行校验
    private List<PrescriptionItemDTO> items;

    // Getters and Setters
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

    public List<PrescriptionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemDTO> items) {
        this.items = items;
    }
} 