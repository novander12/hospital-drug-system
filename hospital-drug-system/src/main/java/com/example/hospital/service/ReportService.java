package com.example.hospital.service;

import com.example.hospital.dto.InventoryReportItemDTO;
import com.example.hospital.dto.DrugConsumptionDTO;
import com.example.hospital.model.Drug;
import com.example.hospital.model.DrugBatch;
import com.example.hospital.model.InventoryTransaction.TransactionType;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class ReportService {

    @Autowired
    private DrugRepository drugRepository;
    
    @Autowired
    private InventoryTransactionRepository transactionRepository;

    /**
     * 生成库存报告数据
     * @return 包含所有药品及其总库存的报告项列表
     */
    public List<InventoryReportItemDTO> generateInventoryReport() {
        // 获取所有药品及其批次信息
        List<Drug> drugsWithBatches = drugRepository.findAllWithBatches();

        // 转换成 DTO 列表
        return drugsWithBatches.stream()
                .map(this::mapDrugToInventoryReportItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将 Drug 实体映射到 InventoryReportItemDTO
     * @param drug Drug 实体（包含批次信息）
     * @return InventoryReportItemDTO
     */
    private InventoryReportItemDTO mapDrugToInventoryReportItemDTO(Drug drug) {
        // 计算总库存
        int totalStock = drug.getBatches().stream()
                .mapToInt(DrugBatch::getQuantity)
                .sum();

        return new InventoryReportItemDTO(
                drug.getId(),
                drug.getName(),
                drug.getSpec(),
                drug.getCategory(),
                drug.getSupplier(),
                totalStock
        );
    }
    
    /**
     * 生成药品消耗统计报告数据
     * @param startDate 开始日期 (inclusive)
     * @param endDate 结束日期 (inclusive)
     * @return 药品消耗 DTO 列表
     */
    public List<DrugConsumptionDTO> generateDrugConsumptionReport(LocalDate startDate, LocalDate endDate) {
        // 将 LocalDate 转换为 LocalDateTime (开始时间的 00:00:00 到结束时间的 23:59:59.999...)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = transactionRepository.findDrugConsumptionSummary(
                startDateTime, 
                endDateTime, 
                TransactionType.OUTBOUND // Specify we want OUTBOUND transactions
        );

        // 将 Object[] 转换为 DrugConsumptionDTO
        List<DrugConsumptionDTO> report = new ArrayList<>();
        for (Object[] result : results) {
            Long drugId = (Long) result[0];
            String drugName = (String) result[1];
            String spec = (String) result[2];
            String category = (String) result[3];
            // The SUM result might be Long or BigDecimal depending on the database/JPA provider
            // Safely convert to Long
            Long totalConsumedQuantity = ((Number) result[4]).longValue(); 
            
            report.add(new DrugConsumptionDTO(drugId, drugName, spec, category, totalConsumedQuantity));
        }

        return report;
    }
} 