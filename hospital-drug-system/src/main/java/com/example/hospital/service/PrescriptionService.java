package com.example.hospital.service;

import com.example.hospital.dto.PrescriptionCreateDTO;
import com.example.hospital.dto.PrescriptionItemDTO;
import com.example.hospital.model.*;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.repository.PrescriptionRepository;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.service.InventoryService;
import com.example.hospital.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final DrugRepository drugRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final OperationLogService operationLogService;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               DrugRepository drugRepository,
                               UserRepository userRepository,
                               InventoryService inventoryService,
                               OperationLogService operationLogService) {
        this.prescriptionRepository = prescriptionRepository;
        this.drugRepository = drugRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.operationLogService = operationLogService;
    }

    /**
     * 获取当前认证用户实体
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    /**
     * 创建新处方
     */
    @Transactional
    public Prescription createPrescription(PrescriptionCreateDTO dto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("无法获取当前用户信息，请先登录");
        }

        Prescription prescription = new Prescription();
        prescription.setPatientName(dto.getPatientName());
        prescription.setPatientIdNumber(dto.getPatientIdNumber());
        prescription.setPatientAge(dto.getPatientAge());
        prescription.setPatientGender(dto.getPatientGender());
        prescription.setPrescribingDoctor(dto.getPrescribingDoctor());
        prescription.setPrescriptionDate(dto.getPrescriptionDate() != null ? dto.getPrescriptionDate() : LocalDate.now());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setCreatedByUser(currentUser);
        prescription.setStatus(Prescription.PrescriptionStatus.PENDING); // 初始状态

        // 处理处方项
        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            Drug drug = drugRepository.findById(itemDto.getDrugId())
                    .orElseThrow(() -> new EntityNotFoundException("未找到 ID 为 " + itemDto.getDrugId() + " 的药品"));
            
            PrescriptionItem item = new PrescriptionItem();
            item.setDrug(drug);
            item.setQuantity(itemDto.getQuantity());
            item.setDosage(itemDto.getDosage());
            item.setFrequency(itemDto.getFrequency());
            item.setNotes(itemDto.getNotes());
            
            prescription.addItem(item); // 添加到处方并建立关联
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        logger.info("用户 '{}' 创建了新的处方，ID: {}", currentUser.getUsername(), savedPrescription.getId());
        return savedPrescription;
    }

    /**
     * 获取所有处方 (后续可添加分页和过滤)
     */
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAllWithItems();
    }

    /**
     * 根据ID获取处方
     */
    public Optional<Prescription> getPrescriptionById(Long id) {
        return prescriptionRepository.findByIdWithItems(id);
    }

    /**
     * 更新处方状态
     * 特别地，当状态变为 DISPENSED 时，会尝试扣减库存。
     */
    @Transactional
    public Prescription updatePrescriptionStatus(Long id, Prescription.PrescriptionStatus newStatus, String remarks) 
        throws EntityNotFoundException, IllegalStateException, IllegalArgumentException {
        
        User currentUser = getCurrentUser();
        Prescription prescription = prescriptionRepository.findByIdWithItems(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到 ID 为 " + id + " 的处方"));

        Prescription.PrescriptionStatus oldStatus = prescription.getStatus();
        if (oldStatus == newStatus) {
            logger.info("处方 {} 状态无需更新，当前状态已为 {}", id, newStatus);
            return prescription; // 状态未改变，直接返回
        }

        if (oldStatus == Prescription.PrescriptionStatus.CANCELLED || oldStatus == Prescription.PrescriptionStatus.DISPENSED) {
             throw new IllegalStateException("无法更新已取消或已发药的处方状态。");
        }
        
        if (newStatus == Prescription.PrescriptionStatus.DISPENSED) {
             if (oldStatus != Prescription.PrescriptionStatus.APPROVED) {
                  throw new IllegalStateException("只有已审核 (APPROVED) 的处方才能进行发药操作。");
             }
            logger.info("开始处理处方 {} 的发药操作...", id);
            dispensePrescription(prescription, currentUser, remarks);
        }
        
        prescription.setStatus(newStatus);
        prescription.setUpdatedAt(LocalDateTime.now());
        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        
        logger.info("用户 '{}' 更新了处方 {} 的状态从 {} 到 {}", 
                    currentUser.getUsername(), id, oldStatus, newStatus);
        return updatedPrescription;
    }

    /**
     * 处理发药逻辑：扣减库存
     */
    private void dispensePrescription(Prescription prescription, User dispensingUser, String remarks) 
        throws IllegalArgumentException {
        
        logger.warn("库存扣减逻辑暂时禁用，等待基于批次的实现。"); // Add a warning log
        
        for (PrescriptionItem item : prescription.getItems()) {
            try {
                String dispenseRemarks = String.format("处方发药 (ID: %d): %s", prescription.getId(), remarks != null ? remarks : "");
                // !!! TEMPORARILY COMMENTED OUT INVENTORY DEDUCTION !!!
                 inventoryService.outboundStock(item.getDrug().getId(), item.getQuantity(), dispenseRemarks);
                // Log actual action instead of disabled status
                logger.info("为处方 {} 中的药品 {} (ID: {}) 记录出库交易 {} 件", 
                           prescription.getId(), item.getDrug().getName(), item.getDrug().getId(), item.getQuantity());
            } catch (IllegalArgumentException e) {
                logger.error("为处方 {} 发药失败：药品 {} (ID: {})。错误: {}", // Removed mention of disabled deduction
                           prescription.getId(), item.getDrug().getName(), item.getDrug().getId(), e.getMessage());
                // Re-throw even if deduction is disabled, in case pre-checks fail (though unlikely now)
                throw new IllegalArgumentException(String.format("发药失败（库存检查相关）：药品 '%s' (需求: %d)", 
                                                item.getDrug().getName(), item.getQuantity()), e);
            }
        }
        logger.info("处方 {} 的发药流程完成", prescription.getId()); // Removed mention of disabled deduction
    }

    // --- 可以添加其他方法，如搜索、删除（如果允许的话）等 ---

} 