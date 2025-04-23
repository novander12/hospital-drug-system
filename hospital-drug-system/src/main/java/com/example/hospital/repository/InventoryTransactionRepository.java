package com.example.hospital.repository;

import com.example.hospital.model.InventoryTransaction;
import com.example.hospital.model.InventoryTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    // 根据药品ID查找交易记录，按时间降序排列
    List<InventoryTransaction> findByDrugIdOrderByTransactionTimeDesc(Long drugId);

    /**
     * 查询指定时间段内各药品的总消耗量（出库量）
     * @param startDate 开始时间 (inclusive)
     * @param endDate 结束时间 (exclusive)
     * @param transactionType 要统计的交易类型 (e.g., OUTBOUND)
     * @return 包含药品ID、名称、规格、类别和总消耗量的 Object[] 列表
     */
    @Query("SELECT t.drug.id, t.drug.name, t.drug.spec, t.drug.category, SUM(ABS(t.quantityChange)) " +
           "FROM InventoryTransaction t " +
           "WHERE t.type = :transactionType AND t.transactionTime >= :startDate AND t.transactionTime < :endDate " +
           "GROUP BY t.drug.id, t.drug.name, t.drug.spec, t.drug.category " +
           "ORDER BY t.drug.name")
    List<Object[]> findDrugConsumptionSummary(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transactionType") TransactionType transactionType
    );

    // 可以根据需要添加其他查询方法，例如按用户、按类型、按时间范围查询
} 