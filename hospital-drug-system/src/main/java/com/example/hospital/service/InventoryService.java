package com.example.hospital.service;

import com.example.hospital.model.Drug;
import com.example.hospital.model.InventoryTransaction;
import com.example.hospital.model.User;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.repository.InventoryTransactionRepository;
import com.example.hospital.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final DrugRepository drugRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public InventoryService(DrugRepository drugRepository, 
                          InventoryTransactionRepository transactionRepository, 
                          UserRepository userRepository) {
        this.drugRepository = drugRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取当前认证用户实体
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Or handle as needed, maybe throw an exception if user must be authenticated
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    /**
     * 获取当前认证用户的用户名字符串，如果未认证则返回默认值
     */
    private String getCurrentUsernameString() {
        User user = getCurrentUser();
        return (user != null) ? user.getUsername() : "System/Unknown";
    }

    /**
     * 核心库存更新与交易记录方法 - OBSOLETE due to batch management
     */
    /*
    @Transactional
    public Drug updateStock(Long drugId, int quantityChange, InventoryTransaction.TransactionType type, String remarks) 
        throws IllegalArgumentException, OptimisticLockException {
        // ... (implementation removed) ...
        return null; // Or throw exception
    }
    */

    /**
     * 记录库存交易 - OBSOLETE/NEEDS REWORK for batches
     */
    /*
    private void recordTransaction(Drug drug, int quantityChange, int stockAfter, 
                                   InventoryTransaction.TransactionType type, String remarks) {
        // ... (implementation depends on how transactions are linked to batches) ...
    }
    */

    // --- 公共接口方法 - OBSOLETE/NEEDS REWORK --- 

    /**
     * 药品入库 - OBSOLETE
     */
    /*
    public Drug inboundStock(Long drugId, int quantity, String remarks) {
        // ... (implementation removed) ...
         return null; 
    }
    */

    /**
     * 药品出库 - NEEDS REWORK - Basic transaction logging added for consumption report
     */
    @Transactional
    public void outboundStock(Long drugId, int quantity, String remarks) 
        throws IllegalArgumentException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("出库数量必须为正数");
        }

        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new EntityNotFoundException("未找到药品 ID: " + drugId));

        // TODO: Implement proper batch selection and deduction logic here
        // For now, just record the transaction for reporting purposes
        // Removed the misleading warning log about not executing deduction
        // logger.warn("药品出库 (ID: {}): 数量 {} - 未执行实际批次库存扣减，仅记录交易。", drugId, quantity);
        
        // --- Start: Record transaction --- 
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setDrug(drug);
        transaction.setType(InventoryTransaction.TransactionType.OUTBOUND);
        transaction.setQuantityChange(-quantity); // Negative for outbound
        // Placeholder stockAfter value as actual batch deduction isn't implemented
        // We could query total stock, but it might be inconsistent without batch logic
        transaction.setStockAfterTransaction(-1); // Use a placeholder like -1 or query (less accurate)
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setUser(getCurrentUser()); 
        transaction.setRemarks(remarks);
        transactionRepository.save(transaction);
        // --- End: Record transaction --- 
        
        // We are not returning the Drug object anymore as we don't update its aggregate stock
        // return drug; 
    }
    
    /**
     * 库存调整 - NEEDS REWORK
     */
    /*
    public Drug adjustStock(Long drugId, int newStock, String remarks) {
       // ... (implementation needs batch adjustment logic) ...
        return null; 
    }
    */
    
     /**
     * 设置药品初始库存 - OBSOLETE
     */
     /*
    public Drug setInitialStock(Long drugId, int initialStock, String remarks) {
       // ... (implementation removed) ...
        return null;
    }
    */
} 