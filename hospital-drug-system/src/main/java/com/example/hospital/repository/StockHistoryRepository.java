package com.example.hospital.repository;

import com.example.hospital.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    
    // 查找指定日期范围内的库存历史
    @Query("SELECT sh FROM StockHistory sh WHERE sh.date >= :startDate ORDER BY sh.date ASC")
    List<StockHistory> findStockHistoryInRange(@Param("startDate") LocalDate startDate);
    
    // 查找指定日期的库存历史
    StockHistory findByDate(LocalDate date);
} 