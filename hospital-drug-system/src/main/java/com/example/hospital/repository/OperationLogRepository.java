package com.example.hospital.repository;

import com.example.hospital.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    // 按操作时间降序查找操作日志
    @Query("SELECT l FROM OperationLog l ORDER BY l.timestamp DESC")
    List<OperationLog> findAllOrderByTimestampDesc();
    
    // 查找特定用户的操作日志
    List<OperationLog> findByUsernameOrderByTimestampDesc(String username);
    
    // 查找特定药品的操作日志
    List<OperationLog> findByDrugIdOrderByTimestampDesc(Long drugId);
} 