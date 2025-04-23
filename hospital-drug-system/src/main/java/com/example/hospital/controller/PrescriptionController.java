package com.example.hospital.controller;

import com.example.hospital.dto.PrescriptionCreateDTO;
import com.example.hospital.dto.UpdateStatusRequestDTO;
import com.example.hospital.model.Prescription;
import com.example.hospital.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    private final PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * 创建新处方
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<?> createPrescription(@Valid @RequestBody PrescriptionCreateDTO createDTO) {
        try {
            Prescription createdPrescription = prescriptionService.createPrescription(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPrescription);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.warn("创建处方失败: {}", e.getMessage());
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(responseBody);
        } catch (IllegalStateException e) {
            logger.warn("创建处方失败: {}", e.getMessage());
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
        } catch (Exception e) {
            logger.error("创建处方时发生意外错误", e);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", "创建处方失败，发生内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    /**
     * 获取所有处方 (包含药品项)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        try {
            List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            logger.error("获取处方列表时发生意外错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * 根据ID获取单个处方 (包含药品项)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPrescriptionById(@PathVariable Long id) {
        Optional<Prescription> prescriptionOpt = prescriptionService.getPrescriptionById(id);
        if (prescriptionOpt.isPresent()) {
            return ResponseEntity.ok(prescriptionOpt.get());
        } else {
            logger.warn("尝试获取不存在的处方，ID: {}", id);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", "处方不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
    }

    /**
     * 更新处方状态 (例如：审核通过、发药、取消)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PHARMACIST') or hasRole('NURSE')")
    public ResponseEntity<?> updatePrescriptionStatus(@PathVariable Long id, 
                                                  @Valid @RequestBody UpdateStatusRequestDTO requestDTO) {
        try {
            Prescription updatedPrescription = prescriptionService.updatePrescriptionStatus(
                id, 
                requestDTO.getNewStatus(),
                requestDTO.getRemarks()
            );
            return ResponseEntity.ok(updatedPrescription);
        } catch (EntityNotFoundException e) {
            logger.warn("尝试更新不存在的处方状态，ID: {}", id);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("更新处方状态失败 (非法状态转换或库存不足)，ID: {}, 原因: {}", id, e.getMessage());
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", e.getMessage());
            return ResponseEntity.status(e instanceof IllegalStateException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (Exception e) {
            logger.error("更新处方状态时发生意外错误, ID: {}", id, e);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", "更新处方状态失败，发生内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
} 