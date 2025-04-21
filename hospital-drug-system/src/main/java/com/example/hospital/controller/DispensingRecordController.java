package com.example.hospital.controller;

import com.example.hospital.model.DispensingRecord;
import com.example.hospital.model.PrescriptionDrug;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dispensing-records")
public class DispensingRecordController {

    private static final Map<Long, DispensingRecord> RECORDS = new HashMap<>();
    private static long nextId = 1;

    static {
        // 添加一些测试数据
        DispensingRecord r1 = new DispensingRecord();
        r1.setId(nextId++);
        r1.setPrescriptionId(1L);
        r1.setPatientName("张三");
        r1.setPatientId("P20240001");
        r1.setDispensedBy("李药师");
        r1.setDispensedTime(LocalDateTime.now().minusHours(5));
        
        List<PrescriptionDrug> drugs1 = new ArrayList<>();
        PrescriptionDrug drug1 = new PrescriptionDrug();
        drug1.setDrugId(1L);
        drug1.setDrugName("降压药");
        drug1.setSpecification("10mg*30片");
        drug1.setQuantity(1);
        drug1.setUnit("盒");
        drug1.setUsage("每日一次，每次一片");
        drugs1.add(drug1);
        r1.setDrugs(drugs1);
        
        RECORDS.put(r1.getId(), r1);
    }

    @GetMapping
    public ResponseEntity<List<DispensingRecord>> getAllRecords(
            @RequestParam(required = false) String prescriptionId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<DispensingRecord> filteredRecords = new ArrayList<>();
        
        for (DispensingRecord record : RECORDS.values()) {
            boolean matches = true;
            
            if (prescriptionId != null && !prescriptionId.isEmpty() && 
                !record.getPrescriptionId().toString().equals(prescriptionId)) {
                matches = false;
            }
            
            if (patientName != null && !patientName.isEmpty() && 
                !record.getPatientName().contains(patientName)) {
                matches = false;
            }
            
            // 此处可以添加日期范围过滤
            
            if (matches) {
                filteredRecords.add(record);
            }
        }
        
        return ResponseEntity.ok(filteredRecords);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispensingRecord> getRecordById(@PathVariable Long id) {
        DispensingRecord record = RECORDS.get(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    @PostMapping
    public ResponseEntity<DispensingRecord> createRecord(@RequestBody DispensingRecord record) {
        record.setId(nextId++);
        record.setDispensedTime(LocalDateTime.now());
        
        RECORDS.put(record.getId(), record);
        return ResponseEntity.ok(record);
    }
} 