package com.example.hospital.controller;

import com.example.hospital.dto.InventoryAdjustRequest;
import com.example.hospital.dto.InventoryRequest;
import com.example.hospital.model.Drug;
import com.example.hospital.model.InventoryTransaction;
import com.example.hospital.repository.InventoryTransactionRepository;
import com.example.hospital.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;
    private final InventoryTransactionRepository transactionRepository; // 用于查询交易记录

    @Autowired
    public InventoryController(InventoryService inventoryService,
                               InventoryTransactionRepository transactionRepository) {
        this.inventoryService = inventoryService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * 药品入库 API - OBSOLETE - Use DrugController POST /api/drugs/{drugId}/batches instead
     */
    /*
    @PostMapping("/inbound")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> inboundStock(@Valid @RequestBody InventoryRequest request) {
        // ... (implementation) ...
    }
    */

    /**
     * 药品出库 API - NEEDS REWORK - Requires batch selection logic
     */
    /*
    @PostMapping("/outbound")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> outboundStock(@Valid @RequestBody InventoryRequest request) {
        // ... (implementation) ...
    }
    */

    /**
     * 库存调整 API - NEEDS REWORK - Requires batch adjustment logic
     */
    /*
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adjustStock(@Valid @RequestBody InventoryAdjustRequest request) {
        // ... (implementation) ...
    }
    */

    /**
     * 获取指定药品的库存交易记录 API (Keep for now, may need adjustment later)
     */
    @GetMapping("/transactions/{drugId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<InventoryTransaction>> getTransactionsByDrugId(@PathVariable Long drugId) {
        try {
            // TODO: Potentially add batch info to transactions or filter them
            List<InventoryTransaction> transactions = transactionRepository.findByDrugIdOrderByTransactionTimeDesc(drugId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("查询药品ID {} 的交易记录时发生错误", drugId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
} 