package com.example.hospital.repository;

import com.example.hospital.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {
    Drug findByName(String name);
    
    /**
     * 根据药品名称进行模糊查询
     * @param keyword 搜索关键词
     * @return 匹配的药品列表
     */
    @Query("SELECT d FROM Drug d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Drug> findByNameContainingIgnoreCase(@Param("keyword") String keyword);
    
    /**
     * 查询库存低于指定阈值的药品
     * @param threshold 库存阈值
     * @return 库存低于阈值的药品列表
     */
    @Query("SELECT d FROM Drug d WHERE d.stock < :threshold ORDER BY d.stock ASC")
    List<Drug> findByStockLessThan(@Param("threshold") Integer threshold);
    
    /**
     * 统计每个类别的药品数量
     * @return 每个类别及其对应的药品数量
     */
    @Query("SELECT d.category as category, COUNT(d) as count FROM Drug d GROUP BY d.category")
    List<Object[]> countByCategory();
    
    // 查找指定天数内即将过期的药品
    @Query("SELECT d FROM Drug d WHERE d.expirationDate <= :endDate ORDER BY d.expirationDate ASC")
    List<Drug> findExpiringDrugs(@Param("endDate") LocalDate endDate);
} 