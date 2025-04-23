package com.example.hospital.service;

import com.example.hospital.dto.DrugBatchCreateDTO;
import com.example.hospital.dto.DrugCreateDTO;
import com.example.hospital.dto.DrugDTO;
import com.example.hospital.model.Drug;
import com.example.hospital.model.DrugBatch;
import com.example.hospital.repository.DrugBatchRepository;
import com.example.hospital.repository.DrugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DrugService {

    private final DrugRepository drugRepository;
    private final DrugBatchRepository drugBatchRepository;
    private final OperationLogService operationLogService;

    @Autowired
    public DrugService(DrugRepository drugRepository, 
                       DrugBatchRepository drugBatchRepository,
                       OperationLogService operationLogService) {
        this.drugRepository = drugRepository;
        this.drugBatchRepository = drugBatchRepository;
        this.operationLogService = operationLogService;
    }

    private DrugDTO convertToDrugDTO(Drug drug) {
        DrugDTO dto = new DrugDTO();
        dto.setId(drug.getId());
        dto.setName(drug.getName());
        dto.setSpec(drug.getSpec());
        dto.setCategory(drug.getCategory());
        dto.setSupplier(drug.getSupplier());
        int totalStock = drug.getBatches().stream()
                             .mapToInt(DrugBatch::getQuantity)
                             .sum();
        dto.setTotalStock(totalStock);
        return dto;
    }

    public List<DrugDTO> getAllDrugs() {
        List<Drug> drugs = drugRepository.findAllWithBatches();
        return drugs.stream()
                    .map(this::convertToDrugDTO)
                    .collect(Collectors.toList());
    }

    public Optional<DrugDTO> getDrugDTOById(Long id) {
        Optional<Drug> drugOpt = drugRepository.findByIdWithBatches(id);
        return drugOpt.map(this::convertToDrugDTO);
    }
    
    public Optional<Drug> getDrugEntityById(Long id) {
        return drugRepository.findById(id);
    }

    @Transactional
    public Drug createDrug(DrugCreateDTO drugCreateDTO) {
        Drug drug = new Drug();
        drug.setName(drugCreateDTO.getName());
        drug.setSpec(drugCreateDTO.getSpec());
        drug.setCategory(drugCreateDTO.getCategory());
        drug.setSupplier(drugCreateDTO.getSupplier());
        Drug savedDrug = drugRepository.save(drug);
        operationLogService.logAddDrug(savedDrug);
        return savedDrug;
    }
    
    @Transactional
    public Drug updateDrug(Long id, DrugCreateDTO drugUpdateDTO) {
        Drug existingDrug = drugRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Drug not found with id: " + id));

        Drug oldDrugState = copyDrugState(existingDrug);

        existingDrug.setName(drugUpdateDTO.getName());
        existingDrug.setSpec(drugUpdateDTO.getSpec());
        existingDrug.setCategory(drugUpdateDTO.getCategory());
        existingDrug.setSupplier(drugUpdateDTO.getSupplier());
        
        Drug updatedDrug = drugRepository.save(existingDrug);
        operationLogService.logUpdateDrug(oldDrugState, updatedDrug);
        return updatedDrug;
    }

    @Transactional
    public DrugBatch addBatchToDrug(Long drugId, DrugBatchCreateDTO batchCreateDTO) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new EntityNotFoundException("Drug not found with id: " + drugId));

        DrugBatch batch = new DrugBatch();
        batch.setDrug(drug);
        batch.setBatchNumber(batchCreateDTO.getBatchNumber());
        batch.setProductionDate(batchCreateDTO.getProductionDate());
        batch.setExpirationDate(batchCreateDTO.getExpirationDate());
        batch.setQuantity(batchCreateDTO.getQuantity());
        batch.setSupplier(batchCreateDTO.getSupplier() != null ? batchCreateDTO.getSupplier() : drug.getSupplier());

        DrugBatch savedBatch = drugBatchRepository.save(batch);
        
        return savedBatch;
    }

    @Transactional
    public void deleteDrug(Long id) {
         Drug drug = drugRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException("Drug not found with id: " + id));
        operationLogService.logDeleteDrug(drug);
        drugRepository.deleteById(id);
    }

    public long countDrugs() {
        return drugRepository.count();
    }

    public List<DrugDTO> searchDrugs(String keyword) {
        List<Drug> drugs = drugRepository.findByNameContainingIgnoreCaseWithBatches(keyword);
        return drugs.stream()
                    .map(this::convertToDrugDTO)
                    .collect(Collectors.toList());
    }
    
    private Drug copyDrugState(Drug drug) {
       Drug copy = new Drug();
       copy.setId(drug.getId());
       copy.setName(drug.getName());
       copy.setSpec(drug.getSpec());
       copy.setCategory(drug.getCategory());
       copy.setSupplier(drug.getSupplier());
       return copy;
    }
    
    /**
     * 获取所有不同的供应商名称
     * @return 供应商名称列表
     */
    public List<String> getDistinctSuppliers() {
        return drugRepository.findDistinctSuppliers();
    }

    public List<DrugBatch> findBatchesByDrugId(Long drugId) {
        return drugBatchRepository.findByDrugIdOrderByExpirationDateAsc(drugId);
    }
    
    /**
     * 查找指定天数内或之前过期的药品，并转换为DTO
     * @param days 天数
     * @return 过期药品的DTO列表
     */
    public List<DrugDTO> findExpiringDrugsDTO(int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<Drug> expiringDrugs = drugRepository.findExpiringDrugs(endDate);
        // 注意：这里的 findExpiringDrugs 可能需要优化以包含批次信息才能计算总库存
        // 或者直接返回 Drug 实体，让 Controller 或前端处理
        // 为了简单起见，我们先返回基础的 DrugDTO，不包含精确的批次过期信息
        return expiringDrugs.stream()
                            .map(this::convertToDrugDTO) // 使用现有的转换方法
                            .collect(Collectors.toList());
    }
} 