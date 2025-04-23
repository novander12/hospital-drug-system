package com.example.hospital.repository;

import com.example.hospital.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {
    
    // 按操作时间降序查找操作日志
    @Query("SELECT l FROM OperationLog l ORDER BY l.timestamp DESC")
    List<OperationLog> findAllOrderByTimestampDesc();
    
    // 查找特定用户的操作日志
    List<OperationLog> findByUsernameOrderByTimestampDesc(String username);
    
    // 查找特定药品的操作日志
    List<OperationLog> findByDrugIdOrderByTimestampDesc(Long drugId);

    // 可以添加一个查询不同操作类型的辅助方法
    @Query("SELECT DISTINCT l.action FROM OperationLog l ORDER BY l.action")
    List<String> findDistinctActionTypes();
} 