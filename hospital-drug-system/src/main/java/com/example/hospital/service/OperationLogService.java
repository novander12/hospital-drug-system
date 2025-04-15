package com.example.hospital.service;

import com.example.hospital.model.Drug;
import com.example.hospital.model.OperationLog;
import com.example.hospital.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;
    
    /**
     * 获取所有操作日志，按时间降序排列
     */
    public List<OperationLog> findAllLogs() {
        return operationLogRepository.findAllOrderByTimestampDesc();
    }
    
    /**
     * 获取指定用户的操作日志
     */
    public List<OperationLog> findLogsByUsername(String username) {
        return operationLogRepository.findByUsernameOrderByTimestampDesc(username);
    }
    
    /**
     * 获取指定药品的操作日志
     */
    public List<OperationLog> findLogsByDrugId(Long drugId) {
        return operationLogRepository.findByDrugIdOrderByTimestampDesc(drugId);
    }
    
    /**
     * 记录一般操作
     * @param username 用户名
     * @param action 操作类型
     * @param details 操作详情
     */
    public void logOperation(String username, String action, String details) {
        OperationLog log = new OperationLog(username, action, null, "系统设置", details);
        operationLogRepository.save(log);
    }
    
    /**
     * 记录添加药品操作
     */
    public void logAddDrug(Drug drug) {
        String username = getCurrentUsername();
        String details = String.format("添加药品，库存：%d", drug.getStock());
        OperationLog log = new OperationLog(username, "ADD", drug.getId(), drug.getName(), details);
        operationLogRepository.save(log);
    }
    
    /**
     * 记录更新药品操作
     */
    public void logUpdateDrug(Drug oldDrug, Drug newDrug) {
        String username = getCurrentUsername();
        StringBuilder details = new StringBuilder("更新药品信息：");
        
        if (!oldDrug.getName().equals(newDrug.getName())) {
            details.append("名称由 ").append(oldDrug.getName())
                   .append(" 改为 ").append(newDrug.getName()).append("；");
        }
        
        if (!oldDrug.getSpec().equals(newDrug.getSpec())) {
            details.append("规格由 ").append(oldDrug.getSpec())
                   .append(" 改为 ").append(newDrug.getSpec()).append("；");
        }
        
        if (!oldDrug.getStock().equals(newDrug.getStock())) {
            details.append("库存由 ").append(oldDrug.getStock())
                   .append(" 改为 ").append(newDrug.getStock()).append("；");
        }
        
        OperationLog log = new OperationLog(username, "UPDATE", newDrug.getId(), newDrug.getName(), details.toString());
        operationLogRepository.save(log);
    }
    
    /**
     * 记录删除药品操作
     */
    public void logDeleteDrug(Drug drug) {
        String username = getCurrentUsername();
        String details = String.format("删除药品，库存：%d", drug.getStock());
        OperationLog log = new OperationLog(username, "DELETE", drug.getId(), drug.getName(), details);
        operationLogRepository.save(log);
    }
    
    /**
     * 记录批量删除药品操作
     */
    public void logBatchDeleteDrugs(List<Drug> drugs) {
        String username = getCurrentUsername();
        String details = String.format("批量删除%d种药品", drugs.size());
        
        OperationLog log = new OperationLog(username, "BATCH_DELETE", null, "多个药品", details);
        operationLogRepository.save(log);
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
} 