package com.example.hospital.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class PrescriptionRequestDTO {

    @NotBlank(message = "病人姓名不能为空")
    @Size(max = 100, message = "病人姓名过长")
    private String patientName;

    private String patientGender;

    private Integer patientAge;

    private String diagnosis;

    @NotBlank(message = "医生姓名不能为空")
    @Size(max = 100, message = "医生姓名过长")
    private String doctorName;

    @NotEmpty(message = "处方明细不能为空")
    @Valid // Enable validation for nested DTOs
    private List<PrescriptionItemDTO> items;

    // Getters and Setters
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public List<PrescriptionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemDTO> items) {
        this.items = items;
    }
} 