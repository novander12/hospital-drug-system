package com.example.hospital.repository;

import com.example.hospital.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {
    Drug findByName(String name);
    
    /**
     * 根据药品名称进行模糊查询
     * @param keyword 搜索关键词
     * @return 匹配的药品列表
     */
    @Query("SELECT d FROM Drug d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Drug> findByNameContainingIgnoreCaseSimple(@Param("keyword") String keyword);
    
    /**
     * 统计每个类别的药品数量
     * @return 每个类别及其对应的药品数量
     */
    @Query("SELECT d.category as category, COUNT(d) as count FROM Drug d GROUP BY d.category")
    List<Object[]> countByCategory();
    
    // 查找指定日期或之前过期的药品（通过批次），并预先加载批次信息
    @Query("SELECT DISTINCT d FROM Drug d LEFT JOIN FETCH d.batches b WHERE b.expirationDate <= :endDate ORDER BY b.expirationDate ASC")
    List<Drug> findExpiringDrugs(@Param("endDate") LocalDate endDate);

    // Find all drugs and eagerly fetch their batches
    @Query("SELECT DISTINCT d FROM Drug d LEFT JOIN FETCH d.batches")
    List<Drug> findAllWithBatches();

    // Find a drug by ID and eagerly fetch its batches
    @Query("SELECT d FROM Drug d LEFT JOIN FETCH d.batches WHERE d.id = :id")
    Optional<Drug> findByIdWithBatches(@Param("id") Long id);
    
    // Search drugs by name (case-insensitive) and eagerly fetch batches
    @Query("SELECT DISTINCT d FROM Drug d LEFT JOIN FETCH d.batches WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Drug> findByNameContainingIgnoreCaseWithBatches(@Param("keyword") String keyword);
    
    /**
     * 获取所有不同的供应商名称
     * @return 供应商名称列表
     */
    @Query("SELECT DISTINCT d.supplier FROM Drug d WHERE d.supplier IS NOT NULL AND d.supplier <> '' ORDER BY d.supplier")
    List<String> findDistinctSuppliers();
} 