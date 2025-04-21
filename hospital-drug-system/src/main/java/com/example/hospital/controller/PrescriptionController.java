package com.example.hospital.controller;

import com.example.hospital.model.Prescription;
import com.example.hospital.model.PrescriptionDrug;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Map<Long, Prescription> PRESCRIPTIONS = new HashMap<>();
    private static long nextId = 1;

    static {
        // 添加一些测试数据
        Prescription p1 = new Prescription();
        p1.setId(nextId++);
        p1.setPatientName("张三");
        p1.setPatientId("P20240001");
        p1.setAge(45);
        p1.setGender("male");
        p1.setDepartment("内科");
        p1.setDoctor("王医生");
        p1.setDiagnosis("高血压");
        p1.setStatus("PENDING");
        p1.setCreateTime(LocalDateTime.now().minusDays(1));
        
        List<PrescriptionDrug> drugs1 = new ArrayList<>();
        PrescriptionDrug drug1 = new PrescriptionDrug();
        drug1.setDrugId(1L);
        drug1.setDrugName("降压药");
        drug1.setSpecification("10mg*30片");
        drug1.setQuantity(1);
        drug1.setUnit("盒");
        drug1.setUsage("每日一次，每次一片");
        drugs1.add(drug1);
        p1.setDrugs(drugs1);
        PRESCRIPTIONS.put(p1.getId(), p1);

        Prescription p2 = new Prescription();
        p2.setId(nextId++);
        p2.setPatientName("李四");
        p2.setPatientId("P20240002");
        p2.setAge(30);
        p2.setGender("female");
        p2.setDepartment("外科");
        p2.setDoctor("李医生");
        p2.setDiagnosis("轻度外伤");
        p2.setStatus("APPROVED");
        p2.setCreateTime(LocalDateTime.now().minusHours(12));
        
        List<PrescriptionDrug> drugs2 = new ArrayList<>();
        PrescriptionDrug drug2 = new PrescriptionDrug();
        drug2.setDrugId(1L);
        drug2.setDrugName("消炎药");
        drug2.setSpecification("5mg*20片");
        drug2.setQuantity(1);
        drug2.setUnit("盒");
        drug2.setUsage("一日三次，饭后服用");
        drugs2.add(drug2);
        p2.setDrugs(drugs2);
        PRESCRIPTIONS.put(p2.getId(), p2);
    }

    @GetMapping
    public ResponseEntity<List<Prescription>> getAllPrescriptions(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<Prescription> filteredPrescriptions = new ArrayList<>();
        
        for (Prescription prescription : PRESCRIPTIONS.values()) {
            boolean matches = true;
            
            if (patientName != null && !patientName.isEmpty() && 
                !prescription.getPatientName().contains(patientName)) {
                matches = false;
            }
            
            if (status != null && !status.isEmpty() && 
                !prescription.getStatus().equals(status)) {
                matches = false;
            }
            
            // 此处可以添加日期范围过滤
            
            if (matches) {
                filteredPrescriptions.add(prescription);
            }
        }
        
        return ResponseEntity.ok(filteredPrescriptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = PRESCRIPTIONS.get(id);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(prescription);
    }

    @PostMapping
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription) {
        prescription.setId(nextId++);
        prescription.setCreateTime(LocalDateTime.now());
        prescription.setStatus("PENDING");
        
        PRESCRIPTIONS.put(prescription.getId(), prescription);
        return ResponseEntity.ok(prescription);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Prescription> cancelPrescription(@PathVariable Long id) {
        Prescription prescription = PRESCRIPTIONS.get(id);
        if (prescription == null) {
            return ResponseEntity.notFound().build();
        }
        
        prescription.setStatus("CANCELLED");
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        // 在实际应用中，这里应该从认证上下文中获取当前用户
        // 并根据用户身份过滤处方
        // 这里简单返回所有处方作为演示
        return ResponseEntity.ok(new ArrayList<>(PRESCRIPTIONS.values()));
    }
} 